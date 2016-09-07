package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * 矩阵分解beta更新规则实现类
 * @author qqx
 *
 */
public class BetaUpdateRule extends UpdateRule {
	/**
	 * beta系数，默认为0.5
	 */
	private double beta = 0.5;
	
	public BetaUpdateRule(double beta) {
		super();
		if(beta<0 || beta>1){
			
		}
		this.beta = beta;
	}

	@Override
	public Matrix executeRule(Matrix A, Matrix X) throws Exception {
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
}
