package cn.cas.cigit.nmf;

import Jama.Matrix;
import cn.cas.cigit.parallel.ParallelMatrixComputer;

/**
 * ������¹������
 * @author qqx
 *
 */
public abstract class UpdateRule {
	/**
	 * ����������ʱ����ĸ����0���Ա���������ֵΪNaN��0��Ԫ�أ����¸�ֵΪgama
	 */
	private static double gama = 1e-5;
	/**
	 * ����������ʵ��
	 */
	protected static ParallelMatrixComputer parallel = null;
	
	public UpdateRule(){
		parallel = new ParallelMatrixComputer();
	}
	
	/**
	 * ִ�зǸ�����ֽ���¹���
	 * @param A �ڽӾ���
	 * @param X ��������
	 * @return
	 * @throws Exception
	 */
	public abstract Matrix executeRule(final Matrix A,final Matrix X) throws Exception;
	
	/**
	 * ���������������������
	 * @param X ��������
	 * @return
	 */
	public Matrix getExpectedMatrix(Matrix X){
		return parallel.executeMultiply(X, X.transpose());
	}
	
	/**
	 * �Էֽ�����еľ��������������ֵΪNaN
	 * @param mat
	 * @return
	 */
	public void reviseMatrix(Matrix mat){
		for(int i=0;i<mat.getRowDimension();i++){
			for(int j=0;j<mat.getColumnDimension();j++){
				if(Double.isNaN(mat.get(i,j))){
					mat.set(i,j,1e-100);
				}else if(mat.get(i, j) < 1e-100){
					mat.set(i, j,1e-100);
				}
			}
		}
		return;
	}
}
