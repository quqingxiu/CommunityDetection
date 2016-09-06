package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * �Ǹ�����ֽ���
 * @author qqx
 *
 */
public class NMFactorization {
	/**
	 * ����������ʱ����ĸ����0���Ա���������ֵΪNaN��0��Ԫ�أ����¸�ֵΪgama
	 */
	private static double gama = 1e-5;
	
	/**
	 * ����ֽ���¹���3���˷����¹���
	 * @param A		Դ����
	 * @param X		��������
	 * @return
	 * @throws Exception 
	 */
	public static Matrix updateMatrixRule_multiplicative(final Matrix A,final Matrix X) throws Exception{
		Matrix axMat = A.times(X);
		Matrix ixMat = getPrincipalDiagonalMatrix(A).times(X);		//A���ԽǾ����X����
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
	
	/**
	 * ����ֽ���¹���2,��n�η�
	 * @param A Դ����
	 * @param X	 ��������
	 * @param alpha	 �����Ŵη�ϵ��
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
					temp.set(i, j, Math.sqrt(temp.get(i, j)));		//��ƽ����
				}else if(alpha == 3){
					temp.set(i, j, Math.cbrt(temp.get(i, j)));		//��������
				}else{
					temp.set(i, j, Math.pow(temp.get(i, j), (double)1/alpha));
				}
			}
		}
		Matrix res = X.arrayTimes(temp);
		return res;
	}
	
	/**
	 * �Ǹ�����ֽ�
	 * @param sourceMat Դ����
	 * @param iterTimes	 ����������
	 * @param e			 �������
	 * @return
	 * @throws Exception 
	 */
	public static Matrix executeNMF(Matrix sourceMat,int K, int iterTimes,double e,Matrix X) throws Exception{
		int times = 0;
		int num = 0;
		double preErr = 0.0;
		while(times < iterTimes){
			Matrix expectedMat = X.times(X.transpose());
			double err = sourceMat.minus(expectedMat).normF();
//			System.out.println("��"+err);
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
		System.out.println("��������times="+times);
		return X;
	}
	
	/**
	 * �Ǹ�����ֽ���¹���1,betaϵ��Ϊ0.5
	 * @param A �ڽӾ���
	 * @param X ��������
	 * @return
	 * @throws Exception
	 */
	public static Matrix updateMatrixRule(Matrix A,Matrix X) throws Exception{
		return updateMatrixRule_beta(A,X,0.5);
	}
	
	/**
	 * �Ǹ�����ֽ���¹���1��betaϵ������
	 * @param A �ڽӾ���
	 * @param X ��������
	 * @param beta ϵ��
	 * @return
	 * @throws Exception
	 */
	public static Matrix updateMatrixRule_beta(final Matrix A,final Matrix X,double beta) throws Exception{
		if(A.getColumnDimension() != X.getRowDimension()){
			throw new Exception("����A���к;���X���в����!");
		}
		int row = A.getRowDimension();
		int col = X.getColumnDimension();
		Matrix res = new Matrix(row,col,1-beta);
		Matrix axMat = A.times(X);
		Matrix xxxMat = X.times(X.transpose()).times(X).times(beta);
		reviseMatrix(xxxMat);			//���������е�Ԫ��
		res.plusEquals(axMat.arrayRightDivide(xxxMat));
		res.arrayTimesEquals(X);
		reviseMatrix(res);
		return res;
	}
	
	/**
	 * �Էֽ�����еľ��������������ֵΪNaN
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
