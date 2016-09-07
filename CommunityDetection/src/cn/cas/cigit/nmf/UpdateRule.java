package cn.cas.cigit.nmf;

import Jama.Matrix;
import cn.cas.cigit.parallel.ParallelMatrixComputer;

/**
 * 矩阵更新规则基类
 * @author qqx
 *
 */
public abstract class UpdateRule {
	/**
	 * 避免矩阵相除时，分母出现0。对被除矩阵中值为NaN和0的元素，重新赋值为gama
	 */
	private static double gama = 1e-5;
	/**
	 * 矩阵并行运算实例
	 */
	protected static ParallelMatrixComputer parallel = null;
	
	public UpdateRule(){
		parallel = new ParallelMatrixComputer();
	}
	
	/**
	 * 执行非负矩阵分解更新规则
	 * @param A 邻接矩阵
	 * @param X 特征矩阵
	 * @return
	 * @throws Exception
	 */
	public abstract Matrix executeRule(final Matrix A,final Matrix X) throws Exception;
	
	/**
	 * 计算特征矩阵的期望矩阵
	 * @param X 特征矩阵
	 * @return
	 */
	public Matrix getExpectedMatrix(Matrix X){
		return parallel.executeMultiply(X, X.transpose());
	}
	
	/**
	 * 对分解过程中的矩阵进行修正，如值为NaN
	 * @param mat
	 * @return
	 */
	public void reviseMatrix(Matrix mat){
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
