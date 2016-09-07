package cn.cas.cigit.parallel;

import java.util.concurrent.RecursiveAction;

/**
 * ����������м�����
 * @author qqx
 *
 */
public class TraversalTask extends RecursiveAction {
	private double[][] arr = null;
	private static final long serialVersionUID = 199104L;
	private int startRowId = 0;	//��ʼ��Id
	private int endRowId = 0;		//������Id
	private int startColId = 0;	//��ʼ��Id
	private int endColId = 0;		//������Id
	private OperatorType operatorType;
	private double gama;
	/**
	 * �����зֵ���ֵ
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
		if((endRowId-startRowId+1) > THRESOLD && (endColId-startColId+1) > THRESOLD){	//��ԭ������в��
			//���в�ֵ�һ������
			int midRowId = (startRowId+endRowId)>>>1;
			//���в�ֵڶ�������
			int midColId = (startColId+endColId)>>>1;
			//�����ĸ�����
			RecursiveAction topLeftTask = new TraversalTask(operatorType,arr,gama, startRowId,midRowId-1,startColId,midColId-1);
			RecursiveAction topRightTask = new TraversalTask(operatorType,arr,gama, startRowId,midRowId-1,midColId,endColId);
			RecursiveAction btmLeftTask = new TraversalTask(operatorType,arr,gama, midRowId,endRowId,startColId,midColId-1);
			RecursiveAction btmRightTask = new TraversalTask(operatorType,arr,gama, midRowId,endRowId,midColId,endColId);
			//ִ������
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
	 * ��������ֵ
	 */
	REVISE("��������ֵ",0),
	/**
	 * �Ծ���ֵ����
	 */
	POWER("�Ծ���ֵ����",1);
	
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
