package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * �˷����¹���ʵ����
 * @author qqx
 *
 */
public class MultiUpdateRule extends UpdateRule {

	@Override
	public Matrix executeRule(Matrix A, Matrix X) throws Exception {
		Matrix axMat = A.times(X);
		Matrix ixMat = getPrincipalDiagonalMatrix(A).times(X);		//A���ԽǾ����X����
		Matrix topMat = axMat.minus(ixMat);
		Matrix xxxMat = X.times(X.transpose()).times(X);
		Matrix xxtMat = X.times(X.transpose());
		Matrix ixxMat = getPrincipalDiagonalMatrix(xxtMat).times(X);
		Matrix buttomMat = xxxMat.minus(ixxMat);
		reviseMatrix(buttomMat);
		Matrix res = X.arrayTimes(topMat.arrayRightDivide(buttomMat));
		reviseMatrix(res);
		return res;
	}
	
	/**
	 * ��ȡ��������ԽǾ���
	 * @return
	 * @throws Exception 
	 */
	public static Matrix getPrincipalDiagonalMatrix(Matrix A) throws Exception{
		if(A.getRowDimension() != A.getColumnDimension()){
			throw new Exception("����A�����Ƿ���");
		}
		Matrix res = Matrix.identity(A.getRowDimension(), A.getColumnDimension());
		for(int i=0,rowSize=A.getRowDimension();i<rowSize;i++){
			res.set(i, i, A.get(i, i));
		}
		return res;
	}
}
