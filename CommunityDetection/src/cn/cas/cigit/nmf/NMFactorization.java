package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * �Ǹ�����ֽ���
 * @author qqx
 *
 */
public class NMFactorization {
	/**
	 * �����Ƿ������ʾ
	 */
	private static boolean finished = false;
	/**
	 * ������¹���ʵ��
	 */
	private UpdateRule rule = null;
	
	public NMFactorization(UpdateRule rule) {
		super();
		this.rule = rule;
	}
	
	/**
	 * �Ǹ�����ֽ�
	 * @param sourceMat Դ����
	 * @param iterTimes	 ����������
	 * @param e			 �������
	 * @return	
	 * @throws Exception 
	 */
	public Matrix executeNMF(Matrix sourceMat,Matrix X,int iterTimes,double e) throws Exception{
		int times = 0;
		int num = 0;
		double preErr = 0.0;
		while(times < iterTimes){
			Matrix expectedMat = rule.getExpectedMatrix(X);
			double err = sourceMat.minus(expectedMat).normF();
			if(times%50 == 0){
				System.out.println("��"+(times)+"����"+err);
			}
			if(Math.abs(err-preErr) < e){
				num++;
			}
			preErr = err;
			if(num == 5){
				break;
			}
			X = rule.executeRule(sourceMat, X);
			times++;
		}
		if(times <= 5){
			finished = true;
		}else{
			finished = false;
		}
		System.out.println("��������times="+times);
		return X;
	}
	
	public static boolean isFinished() {
		return finished;
	}
}
