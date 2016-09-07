package cn.cas.cigit.parallel;

import java.util.concurrent.ForkJoinPool;

import Jama.Matrix;

/**
 * �����м�����
 * @author qqx
 *
 */
public class ParallelMatrixComputer {
	private ForkJoinPool pool;

	/**
	 * ִ�о���˷�
	 * @param leftMat �����
	 * @param rightMat �Ҿ���
	 * @return
	 */
	public Matrix executeMultiply(Matrix leftMat,Matrix rightMat){
		int processors = Runtime.getRuntime().availableProcessors();
		pool = new ForkJoinPool(processors);
		int rowDim = leftMat.getRowDimension();
		int colDim = rightMat.getColumnDimension();
		double[][] resArr = new double[rowDim][colDim];
		//�Եڶ���������һ��ת�ã�ʹ�����ʱ��Ѱַ��Ϊ��Ѱַ
		pool.invoke(new MultiplyTask(leftMat.getArray(), rightMat.transpose().getArray(),resArr, 0,rowDim-1,0,colDim-1));
		return Matrix.constructWithCopy(resArr);
	}
	
	/**
	 * ִ�о���ֵ��n�η�����
	 * @param mat ����
	 * @param n �η���
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
		System.out.println("���м����ʱ��"+(end-start));
//		Matrix res= oneMat.times(anotherMat);
		Matrix res= anotherMat.copy();
		for(int i=0;i<res.getRowDimension();i++){
			for(int j=0;j<res.getColumnDimension();j++){
				res.set(i, j, Math.cbrt(res.get(i, j)));		//��ƽ����
			}
		}
		start = System.currentTimeMillis();
		System.out.println("�ǲ��м����ʱ��"+(start-end));
		
		if(mm.getRowDimension() != res.getRowDimension() || mm.getColumnDimension() != res.getColumnDimension()){
			System.out.println("ά�Ȳ�һ�£�����������ȣ�");
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
			System.out.println("�����������!");
		}else{
			System.out.println("�����������!");
		}
	}

}
