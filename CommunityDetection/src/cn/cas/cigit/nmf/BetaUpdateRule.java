package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * ����ֽ�beta���¹���ʵ����
 * @author qqx
 *
 */
public class BetaUpdateRule extends UpdateRule {
	/**
	 * betaϵ����Ĭ��Ϊ0.5
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
			throw new Exception("����A���к;���X���в����!");
		}
		int row = A.getRowDimension();
		int col = X.getColumnDimension();
		Matrix res = new Matrix(row,col,1-beta);
//		Matrix axMat = A.times(X);
//		Matrix xxxMat = X.times(X.transpose()).times(X).times(beta);
		Matrix axMat = parallel.executeMultiply(A, X);
		Matrix xxtMat = parallel.executeMultiply(X, X.transpose());
		Matrix xxxMat = parallel.executeMultiply(xxtMat, X).times(beta);
		reviseMatrix(xxxMat);			//���������е�Ԫ��
		res.plusEquals(axMat.arrayRightDivide(xxxMat));
		res.arrayTimesEquals(X);
		reviseMatrix(res);
		return res;
	}
}
