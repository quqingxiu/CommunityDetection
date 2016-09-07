package cn.cas.cigit.nmf;

import Jama.Matrix;

/**
 * 非负矩阵分解类
 * @author qqx
 *
 */
public class NMFactorization {
	/**
	 * 迭代是否结束标示
	 */
	private static boolean finished = false;
	/**
	 * 矩阵更新规则实例
	 */
	private UpdateRule rule = null;
	
	public NMFactorization(UpdateRule rule) {
		super();
		this.rule = rule;
	}
	
	/**
	 * 非负矩阵分解
	 * @param sourceMat 源矩阵
	 * @param iterTimes	 最大迭代次数
	 * @param e			 误差上限
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
//			System.out.println("第"+(times)+"次误差："+err);
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
		System.out.println("迭代次数times="+times);
		return X;
	}
	
	public static boolean isFinished() {
		return finished;
	}
}
