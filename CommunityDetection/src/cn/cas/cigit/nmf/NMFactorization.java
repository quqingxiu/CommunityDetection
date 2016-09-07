package cn.cas.cigit.nmf;


import Jama.Matrix;
import cn.cas.cigit.parallel.ParallelMatrixComputer;

/**
 * 非负矩阵分解类
 * @author qqx
 *
 */
public class NMFactorization {
	/**
	 * 避免矩阵相除时，分母出现0。对被除矩阵中值为NaN和0的元素，重新赋值为gama
	 */
	private static double gama = 1e-5;
	/**
	 * 矩阵并行运算实例
	 */
	private static ParallelMatrixComputer parallel = new ParallelMatrixComputer();
	/**
	 * 迭代是否结束标示
	 */
	private static boolean finished = false;
	
	/**
	 * 矩阵分解更新规则3，乘法更新规则
	 * @param A		源矩阵
	 * @param X		特征矩阵
	 * @return
	 * @throws Exception 
	 */
	public static Matrix updateMatrixRule_multiplicative(final Matrix A,final Matrix X) throws Exception{
		Matrix axMat = A.times(X);
		Matrix ixMat = getPrincipalDiagonalMatrix(A).times(X);		//A主对角矩阵乘X矩阵
		Matrix topMat = axMat.minus(ixMat);
		Matrix xxxMat = X.times(X.transpose()).times(X);
		Matrix xxtMat = X.times(X.transpose());
		Matrix ixxMat = getPrincipalDiagonalMatrix(xxtMat).times(X);
		Matrix buttomMat = xxxMat.minus(ixxMat);
		reviseMatrix(buttomMat);
		Matrix res = X.arrayTimes(topMat.arrayRightDivide(buttomMat));
//		reviseMatrix(res);
		return res;
	}
	
	/**
	 * 获取矩阵的主对角矩阵
	 * @return
	 * @throws Exception 
	 */
	public static Matrix getPrincipalDiagonalMatrix(Matrix A) throws Exception{
		if(A.getRowDimension() != A.getColumnDimension()){
			throw new Exception("矩阵A必须是方阵！");
		}
		Matrix res = Matrix.identity(A.getRowDimension(), A.getColumnDimension());
		for(int i=0,rowSize=A.getRowDimension();i<rowSize;i++){
			res.set(i, i, A.get(i, i));
		}
		return res;
	}
	
	/**
	 * 矩阵分解更新规则2,开n次方
	 * @param A 源矩阵
	 * @param X	 特征矩阵
	 * @param alpha	 开根号次方系数
	 * @return
	 */
	public static Matrix updateMatrixRule_alpha(final Matrix A,final Matrix X,int alpha){
		Matrix axMat = A.times(X);
		Matrix xxxMat = X.times(X.transpose()).times(X);
		reviseMatrix(xxxMat);
		Matrix temp = axMat.arrayRightDivide(xxxMat);
		for(int i=0;i<temp.getRowDimension();i++){
			for(int j=0;j<temp.getColumnDimension();j++){
				if(alpha == 2){
					temp.set(i, j, Math.sqrt(temp.get(i, j)));		//开平方根
				}else if(alpha == 3){
					temp.set(i, j, Math.cbrt(temp.get(i, j)));		//开立方根
				}else{
					temp.set(i, j, Math.pow(temp.get(i, j), (double)1/alpha));
				}
			}
		}
		Matrix res = X.arrayTimes(temp);
		return res;
	}
	
	/**
	 * 非负矩阵分解
	 * @param sourceMat 源矩阵
	 * @param iterTimes	 最大迭代次数
	 * @param e			 误差上限
	 * @param iterFlag	 迭代是否完成
	 * @return	
	 * @throws Exception 
	 */
	public static Matrix executeNMF(Matrix sourceMat,int K, int iterTimes,double e,Matrix X) throws Exception{
		int times = 0;
		int num = 0;
		double preErr = 0.0;
		while(times < iterTimes){
			Matrix expectedMat = parallel.executeMultiply(X, X.transpose());
			double err = sourceMat.minus(expectedMat).normF();
			System.out.println("第"+(times)+"次误差："+err);
			if(Math.abs(err-preErr) < e){
				num++;
			}
			preErr = err;
			if(num == 5){
				break;
			}
			X = updateMatrixRule_beta(sourceMat, X,0.5);
//			X = updateMatrixRule_alpha(sourceMat, X,3);
//			X = updateMatrixRule_multiplicative(sourceMat, X);
			times++;
		}
		if(num <= 5){
			finished = true;
		}else{
			finished = false;
		}
		System.out.println("迭代次数times="+times);
		return X;
	}
	
	public static boolean isFinished() {
		return finished;
	}

	/**
	 * 非负矩阵分解更新规则1,beta系数为0.5
	 * @param A 邻接矩阵
	 * @param X 特征矩阵
	 * @return
	 * @throws Exception
	 */
	public static Matrix updateMatrixRule(Matrix A,Matrix X) throws Exception{
		return updateMatrixRule_beta(A,X,0.5);
	}
	
	/**
	 * 非负矩阵分解更新规则1，beta系数更新
	 * @param A 邻接矩阵
	 * @param X 特征矩阵
	 * @param beta 系数
	 * @return
	 * @throws Exception
	 */
	public static Matrix updateMatrixRule_beta(final Matrix A,final Matrix X,double beta) throws Exception{
		if(A.getColumnDimension() != X.getRowDimension()){
			throw new Exception("矩阵A的列和矩阵X的行不相等!");
		}
		int row = A.getRowDimension();
		int col = X.getColumnDimension();
		Matrix res = new Matrix(row,col,1-beta);
//		Matrix axMat = A.times(X);
//		Matrix xxxMat = X.times(X.transpose()).times(X).times(beta);
		Matrix axMat = parallel.executeMultiply(A, X);
		Matrix xxtMat = parallel.executeMultiply(X, X.transpose());
		Matrix xxxMat = parallel.executeMultiply(xxtMat, X).times(beta);
		reviseMatrix(xxxMat);			//修正矩阵中的元素
		res.plusEquals(axMat.arrayRightDivide(xxxMat));
		res.arrayTimesEquals(X);
		reviseMatrix(res);
		return res;
	}
	
	/**
	 * 对分解过程中的矩阵进行修正，如值为NaN
	 * @param mat
	 * @return
	 */
	public static void reviseMatrix(Matrix mat){
		for(int i=0;i<mat.getRowDimension();i++){
			for(int j=0;j<mat.getColumnDimension();j++){
				if(Double.isNaN(mat.get(i,j)) || mat.get(i,j) == 0){
					mat.set(i,j,gama);
				}else if(mat.get(i, j) < 1e-30){
					mat.set(i, j,1e-30);
				}
			}
		}
		return;
	}
}
