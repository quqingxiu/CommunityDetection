package cn.cas.cigit.parallel;

import java.util.concurrent.RecursiveAction;

/**
 * 矩阵遍历并行计算类
 * @author qqx
 *
 */
public class TraversalTask extends RecursiveAction {
	private double[][] arr = null;
	private static final long serialVersionUID = 199104L;
	private int startRowId = 0;	//开始行Id
	private int endRowId = 0;		//结束行Id
	private int startColId = 0;	//开始列Id
	private int endColId = 0;		//结束列Id
	private OperatorType operatorType;
	private double gama;
	/**
	 * 矩阵切分的阈值
	 */
	private static final int THRESOLD = 50;

	public TraversalTask(OperatorType operatorType,double[][] arr,double gama,int startRowId,int endRowId,int startColId,int endColId){
		this.arr = arr;
		this.startRowId = startRowId;
		this.endRowId = endRowId;
		this.startColId = startColId;
		this.endColId = endColId;
		this.operatorType = operatorType;
		this.gama = gama;
	}
	
	@Override
	protected void compute() {
		if((endRowId-startRowId+1) > THRESOLD && (endColId-startColId+1) > THRESOLD){	//对原矩阵进行拆分
			//按行拆分第一个矩阵
			int midRowId = (startRowId+endRowId)>>>1;
			//按列拆分第二个矩阵
			int midColId = (startColId+endColId)>>>1;
			//定义四个任务
			RecursiveAction topLeftTask = new TraversalTask(operatorType,arr,gama, startRowId,midRowId-1,startColId,midColId-1);
			RecursiveAction topRightTask = new TraversalTask(operatorType,arr,gama, startRowId,midRowId-1,midColId,endColId);
			RecursiveAction btmLeftTask = new TraversalTask(operatorType,arr,gama, midRowId,endRowId,startColId,midColId-1);
			RecursiveAction btmRightTask = new TraversalTask(operatorType,arr,gama, midRowId,endRowId,midColId,endColId);
			//执行任务
			invokeAll(topLeftTask,topRightTask,btmLeftTask,btmRightTask);
		}else if((endRowId-startRowId+1) > THRESOLD && (endColId-startColId+1) <=THRESOLD){
			int midRowId = (startRowId+endRowId)>>>1;
			RecursiveAction topTask = new TraversalTask(operatorType,arr,gama, startRowId,midRowId-1,startColId,endColId);
			RecursiveAction btmTask = new TraversalTask(operatorType,arr,gama, midRowId,endRowId,startColId,endColId);
			invokeAll(topTask,btmTask);
		}else if((endRowId-startRowId+1) <= THRESOLD && (endColId-startColId+1) >THRESOLD){
			int midColId = (startColId+endColId)>>>1;
			RecursiveAction leftTask = new TraversalTask(operatorType,arr,gama, startRowId,endRowId,startColId,midColId-1);
			RecursiveAction rightTask = new TraversalTask(operatorType,arr,gama, startRowId,endRowId,midColId,endColId);
			invokeAll(leftTask,rightTask);
		}else{
			for(int rowId=startRowId; rowId<=endRowId; rowId++){
				for(int colId=startColId; colId<=endColId; colId++){
					if(operatorType == OperatorType.POWER){
						arr[rowId][colId] =Math.pow(arr[rowId][colId], gama);
					}
				}
			}
		}
	}

}

enum OperatorType{
	/**
	 * 修正矩阵值
	 */
	REVISE("修正矩阵值",0),
	/**
	 * 对矩阵值开方
	 */
	POWER("对矩阵值开方",1);
	
	private String name;
	private int index;
	
	private OperatorType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
}
