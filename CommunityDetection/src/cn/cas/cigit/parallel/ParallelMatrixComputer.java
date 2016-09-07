package cn.cas.cigit.parallel;

import java.util.concurrent.ForkJoinPool;

import Jama.Matrix;

/**
 * 矩阵并行计算类
 * @author qqx
 *
 */
public class ParallelMatrixComputer {
	private ForkJoinPool pool;

	/**
	 * 执行矩阵乘法
	 * @param leftMat 左矩阵
	 * @param rightMat 右矩阵
	 * @return
	 */
	public Matrix executeMultiply(Matrix leftMat,Matrix rightMat){
		int processors = Runtime.getRuntime().availableProcessors();
		pool = new ForkJoinPool(processors);
		int rowDim = leftMat.getRowDimension();
		int colDim = rightMat.getColumnDimension();
		double[][] resArr = new double[rowDim][colDim];
		//对第二个矩阵做一次转置，使得相乘时列寻址变为行寻址
		pool.invoke(new MultiplyTask(leftMat.getArray(), rightMat.transpose().getArray(),resArr, 0,rowDim-1,0,colDim-1));
		return Matrix.constructWithCopy(resArr);
	}
	
	/**
	 * 执行矩阵值求n次方操作
	 * @param mat 矩阵
	 * @param n 次方数
	 * @return
	 */
	public Matrix powerMatrix(Matrix mat,double n){
		if(n == 1){
			return mat;
		}
		int processors = Runtime.getRuntime().availableProcessors();
		pool = new ForkJoinPool(processors);
		int colDim = mat.getColumnDimension();
		int rowDim = mat.getRowDimension();
		double[][] resArr = mat.getArrayCopy();
		pool.invoke(new TraversalTask(OperatorType.POWER,resArr,n, 0,rowDim-1,0,colDim-1));
		return Matrix.constructWithCopy(resArr);
	}
	
	
	
	public static void main(String[] args) {
//		Matrix oneMat = new Matrix(3000,8000,0.5);
		Matrix anotherMat = new Matrix(3000,2,0.671);
		long start = System.currentTimeMillis();
		ParallelMatrixComputer multi = new ParallelMatrixComputer();
//		Matrix mm = multi.executeMultiply(oneMat, anotherMat);
		Matrix mm = multi.powerMatrix(anotherMat, (double)1/3);
		
		long end = System.currentTimeMillis();
		System.out.println("并行计算耗时："+(end-start));
//		Matrix res= oneMat.times(anotherMat);
		Matrix res= anotherMat.copy();
		for(int i=0;i<res.getRowDimension();i++){
			for(int j=0;j<res.getColumnDimension();j++){
				res.set(i, j, Math.cbrt(res.get(i, j)));		//开平方根
			}
		}
		start = System.currentTimeMillis();
		System.out.println("非并行计算耗时："+(start-end));
		
		if(mm.getRowDimension() != res.getRowDimension() || mm.getColumnDimension() != res.getColumnDimension()){
			System.out.println("维度不一致，两个矩阵不相等！");
		}
		boolean flag = true;
		for(int i=0;i<mm.getRowDimension();i++){
			for(int j=0;j<mm.getColumnDimension();j++){
				if(mm.get(i, j) != res.get(i, j)){
					flag = false;
				}
			}
		}
		if(flag){
			System.out.println("两个矩阵相等!");
		}else{
			System.out.println("两个矩阵不相等!");
		}
	}

}
