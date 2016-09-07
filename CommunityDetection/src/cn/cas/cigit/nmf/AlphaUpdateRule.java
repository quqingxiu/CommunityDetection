package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * 矩阵分解alpha更新规则实现类
 * @author qqx
 *
 */
public class AlphaUpdateRule extends UpdateRule {
	/**
	 * alpha系数，默认为1
	 */
	private double alpha = 1;
	
	public AlphaUpdateRule(double alpha){
		super();
		this.alpha = alpha;
	}
	
	@Override
	public Matrix executeRule(Matrix A, Matrix X) throws Exception {
//		Matrix axMat = A.times(X);
//		Matrix xxxMat = X.times(X.transpose()).times(X);
		Matrix axMat = parallel.executeMultiply(A, X);
		Matrix xxtMat = parallel.executeMultiply(X, X.transpose());
		Matrix xxxMat = parallel.executeMultiply(xxtMat, X);
		reviseMatrix(xxxMat);
		Matrix temp = axMat.arrayRightDivide(xxxMat);
		temp = parallel.powerMatrix(temp, (double)1/alpha);
//		for(int i=0;i<temp.getRowDimension();i++){
//			for(int j=0;j<temp.getColumnDimension();j++){
//				if(alpha == 2){
//					temp.set(i, j, Math.sqrt(temp.get(i, j)));		//开平方根
//				}else if(alpha == 3){
//					temp.set(i, j, Math.cbrt(temp.get(i, j)));		//开立方根
//				}else{
//					temp.set(i, j, Math.pow(temp.get(i, j), (double)1/alpha));
//				}
//			}
//		}
		Matrix res = X.arrayTimes(temp);
		reviseMatrix(res);
		return res;
	}
}
