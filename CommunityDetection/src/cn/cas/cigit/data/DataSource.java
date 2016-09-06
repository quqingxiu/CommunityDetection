package cn.cas.cigit.data;

import java.io.File;
import java.util.List;
import java.util.Set;

import Jama.Matrix;
import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Network;
import cn.cas.cigit.model.Node;
import cn.cas.cigit.parse.DatasetParse;

/**
 * ���ݵ�����
 * 
 * @author qqx
 *
 */
public class DataSource {
	private Network netWork = null;
	private DatasetParse datasetParse = null;
	
	public DataSource(DatasetParse datasetParse){
		this.datasetParse = datasetParse;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(System.getProperty("user.dir"));
	}
	
	/**
	 * ���ļ��ж�ȡ���ݣ������ɾ���
	 * @param filePath
	 * @return
	 */
	public static Matrix getMatrixFromFile(String filePath){
		Matrix mat = null;
		try {
			List<String> list = FileUtils.readFileByLine(new File(filePath));
			mat = new Matrix(list.size(),list.get(0).trim().split("  ").length,0);
			for(int i=0; i<list.size(); i++){
				String str = list.get(i).trim();
				String[] sArr = str.split("  ");
				for(int j=0; j<sArr.length; j++){
					mat.set(i, j, Double.valueOf(sArr[j]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mat;
	}
	
	/**
	 * ��ʼ������
	 * @return
	 */
	private boolean initialNetWork(List<Node> nodeSet,Set<String> labels,List<Edge> edgeSet){
		this.netWork = new Network(labels);
		int nodeSize = nodeSet.size();
		for(int k=0;k<nodeSize;k++){
			netWork.addNode(nodeSet.get(k));
		}
		Matrix adjacencyMat = new Matrix(nodeSize,nodeSize,0);
		for(int i=0,edgeNum=edgeSet.size();i<edgeNum;i++){
			adjacencyMat.set(edgeSet.get(i).getSourceId(), edgeSet.get(i).getDestinationId(), 1);
			adjacencyMat.set(edgeSet.get(i).getDestinationId(), edgeSet.get(i).getSourceId(), 1);
		}
		netWork.setAdjacencyMat(adjacencyMat);
		return true;
	}
	
	/**
	 * ˢ������ʵ��������Ϊ��ʼת̬
	 * @return
	 */
	public Network refreshNetwork(){
		if(netWork == null){
			datasetParse.parseDatasetFile();
			initialNetWork(datasetParse.getNodeSet(),datasetParse.getLabels(),datasetParse.getEdgeSet());
		}else{
			initialNetWork(datasetParse.getNodeSet(),datasetParse.getLabels(),datasetParse.getEdgeSet());
		}
		return netWork;
	}
	
	/**
	 * ��ȡ����ʵ��
	 * @return
	 */
	public Network getNetWork() {
		datasetParse.parseDatasetFile();
//		showInfomationOfDataset();
		initialNetWork(datasetParse.getNodeSet(),datasetParse.getLabels(),datasetParse.getEdgeSet());
		return netWork;
	}
	
	public void showInfomationOfDataset(){
		System.out.println("���ݼ��������"+datasetParse.getNodeSet().size());
		System.out.println("���ݼ�������"+datasetParse.getEdgeSet().size());
	}
}
