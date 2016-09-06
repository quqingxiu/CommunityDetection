package cn.cas.cigit.parallel;

import java.util.concurrent.ForkJoinPool;

import Jama.Matrix;

public class ParallelMatrixComputer {
	private ForkJoinPool pool;

	public Matrix executeMultiply(Matrix oneMat,Matrix anotherMat){
		int processors = Runtime.getRuntime().availableProcessors();
		pool = new ForkJoinPool(processors);
		int rowDim = oneMat.getRowDimension();
		int colDim = anotherMat.getColumnDimension();
		double[][] resArr = new double[rowDim][colDim];
		//�Եڶ���������һ��ת�ã�ʹ�����ʱ��Ѱַ��Ϊ��Ѱַ
		pool.invoke(new MultiplyTask(oneMat.getArray(), anotherMat.transpose().getArray(),resArr, 0,rowDim-1,0,colDim-1));
		return Matrix.constructWithCopy(resArr);
	}
	
	public static void main(String[] args) {
		Matrix oneMat = new Matrix(3000,8000,0.5);
		Matrix anotherMat = new Matrix(8000,800,0.671);
		long start = System.currentTimeMillis();
		ParallelMatrixComputer multi = new ParallelMatrixComputer();
		Matrix mm = multi.executeMultiply(oneMat, anotherMat);
		long end = System.currentTimeMillis();
		System.out.println("���м����ʱ��"+(end-start));
		Matrix res= oneMat.times(anotherMat);
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
