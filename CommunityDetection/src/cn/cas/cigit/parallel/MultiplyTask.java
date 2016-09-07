package cn.cas.cigit.parallel;

import java.util.concurrent.RecursiveAction;

/**
 * 矩阵乘法并行计算实现类
 * @author qqx
 *
 */
public class MultiplyTask extends RecursiveAction {
	private static final long serialVersionUID = 1L;
	/**
	 * 矩阵切分的阈值
	 */
	private static final int THRESOLD = 100;
	
	private double[][] leftMat;
	private double[][] rightMat;
	private double[][] resMat;
	private int startRowId = 0;	//开始行Id
	private int endRowId = 0;		//结束行Id
	private int startColId = 0;	//开始列Id
	private int endColId = 0;		//结束列Id
	
	/**
	 * 构造函数
	 * @param leftMat 左矩阵
	 * @param rightMat 右矩阵
	 * @param resMat  返回结果矩阵
	 * @param startRowId
	 * @param endRowId
	 * @param startColId
	 * @param endColId
	 */
	public MultiplyTask(double[][] leftMat,double[][] rightMat,double[][] resMat,int startRowId,int endRowId,int startColId,int endColId) {
		this.leftMat = leftMat;
		this.rightMat = rightMat;
		this.resMat = resMat;
		this.startRowId = startRowId;
		this.endRowId = endRowId;
		this.startColId = startColId;
		this.endColId = endColId;
	}

	@Override
	protected void compute() {
		if((endRowId-startRowId+1) > THRESOLD && (endColId-startColId+1) > THRESOLD){	//对原矩阵进行拆分
			//按行拆分第一个矩阵
			int midRowId = (startRowId+endRowId)>>>1;
			//按列拆分第二个矩阵
			int midColId = (startColId+endColId)>>>1;
			//定义四个任务
			RecursiveAction topLeftTask = new MultiplyTask(leftMat, rightMat,resMat, startRowId,midRowId-1,startColId,midColId-1);
			RecursiveAction topRightTask = new MultiplyTask(leftMat, rightMat,resMat, startRowId,midRowId-1,midColId,endColId);
			RecursiveAction btmLeftTask = new MultiplyTask(leftMat, rightMat,resMat, midRowId,endRowId,startColId,midColId-1);
			RecursiveAction btmRightTask = new MultiplyTask(leftMat, rightMat,resMat, midRowId,endRowId,midColId,endColId);
			//执行任务
			invokeAll(topLeftTask,topRightTask,btmLeftTask,btmRightTask);
		}else if((endRowId-startRowId+1) > THRESOLD && (endColId-startColId+1) <=THRESOLD){
			int midRowId = (startRowId+endRowId)>>>1;
			RecursiveAction topTask = new MultiplyTask(leftMat, rightMat,resMat, startRowId,midRowId-1,startColId,endColId);
			RecursiveAction btmTask = new MultiplyTask(leftMat, rightMat,resMat, midRowId,endRowId,startColId,endColId);
			invokeAll(topTask,btmTask);
		}else if((endRowId-startRowId+1) <= THRESOLD && (endColId-startColId+1) >THRESOLD){
			int midColId = (startColId+endColId)>>>1;
			RecursiveAction leftTask = new MultiplyTask(leftMat, rightMat,resMat, startRowId,endRowId,startColId,midColId-1);
			RecursiveAction rightTask = new MultiplyTask(leftMat, rightMat,resMat, startRowId,endRowId,midColId,endColId);
			invokeAll(leftTask,rightTask);
		}else{
			int elementSize = leftMat[0].length;
			for(int rowId=startRowId; rowId<=endRowId; rowId++){
				for(int colId=startColId; colId<=endColId; colId++){
					double value = 0;
					for(int n=0;n<elementSize;n++){
						value += leftMat[rowId][n]*rightMat[colId][n];
					}
					resMat[rowId][colId] = value;
				}
			}
		}
	}
}