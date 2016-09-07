package cn.cas.cigit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;
import cn.cas.cigit.data.CollectionUtil;
import cn.cas.cigit.nmf.NMFactorization;

/**
 * ����ʵ����
 * @author qqx
 *
 */
public class Network {
	/**
	 * �����������ĸ���
	 */
	private int K = 2;
	/**
	 * ѡ����Ϊ��ʶ������
	 */
	private int SELECTED_SIZE = 1;
	/**
	 * ����������������
	 */
	private String[] communityLabels = null;
	/**
	 * �����ڽӾ���
	 */
	private Matrix adjacencyMat = null;
	/**
	 * ����㼯
	 */
	private Map<Integer,Node> nodeSet = new HashMap<Integer,Node>();
	/**
	 * ��¼�����е�����
	 */
	private Map<Integer,Community> CommsOfNetwork = new HashMap<Integer, Community>();
	/**
	 * ��¼��ӻ�ɾ��������
	 */
	private Set<Edge> humanLabeling = new HashSet<Edge>();
	
	public Network(Set<String> labels) {
		this.communityLabels = new String[labels.size()];
		int commId = 0;
		for(String label:labels){
			communityLabels[commId] = label;
			CommsOfNetwork.put(commId, new Community(commId));
			commId++;
		}
		this.K = labels.size();
	}
	
	public static void main(String[] args) {
	}
	
	/**
	 * �������
	 * @param usedPercent ʹ����Ϊ��ʶ���ӵİٷֱ�
	 * @param selectedSize ÿ�ε����޸ĵ�������
	 * @throws Exception
	 */
	public double communityDetection(double usedPercent,int selectedSize) throws Exception{
		return communityDetection(usedPercent,selectedSize,3000,1e-8);
	}
	
	/**
	 * �������
	 * @param usedPercent ʹ����Ϊ��ʶ���ӵİٷֱ�
	 * @param selectedSize ÿ�ε����޸ĵ�������
	 * @param maxIter ����������
	 * @param maxError ������
	 * @throws Exception
	 */
	public double communityDetection(double usedPercent,int selectedSize,int maxIter,double maxError) throws Exception{
		System.out.println("\n��ʼִ����������㷨.........");
		int iterNoChangeNum = 0;	//û�н��ı�����������������������
		int times = 0;
		this.SELECTED_SIZE = selectedSize;
		Matrix X = Matrix.random(adjacencyMat.getRowDimension(), K);
		X.arrayTimesEquals(new Matrix(adjacencyMat.getRowDimension(),K,0.01));
		
		double nmi = 0;
		while(times < 100){
			System.out.println("��"+(times+1)+"�ε�����");
			X = NMFactorization.executeNMF(adjacencyMat,K, maxIter,maxError,X);
			boolean modifyFlag = getConmunityOfNode(X.getArray());
			nmi = calculateNMI();		//����ִ�н���ı�׼����Ϣֵ
			if(modifyFlag){
				iterNoChangeNum = 0;
			}else{
				iterNoChangeNum ++;
			}
			if(iterNoChangeNum >= 5){	//����5�ε���û�н��ı�����������������ֽ�
				break;
			}
			double[] entropys = calcEntropyOfNode(X.getArray());
			modifyNetworkByConnectionStrategy(entropys,(int)(usedPercent*0.01*nodeSet.size()*(nodeSet.size()-1)));
			times++;
		}
		return nmi;
	}
	
	/**
	 * ����ڵ����
	 * @param arr ��ʾX����
	 */
	public double[] calcEntropyOfNode(double[][] arr){
		double[] entropys = new double[arr.length];
		double[] rowSum = new double[arr.length];
		for(int i=0;i<arr.length;i++){
			double sum = 0.0;
			for(double item:arr[i]){
				sum += item;
			}
			rowSum[i] = sum;
		}
		for(int i=0;i<arr.length;i++){
			double entropy = 0.0;
			for(double item:arr[i]){
				double pro = item/rowSum[i];
				entropy -= pro*Math.log(pro);
			}
			if(Double.isNaN(entropy)){
				System.out.println("����NaN, "+CollectionUtil.toString(arr[i])+", sum: "+rowSum[i]);
			}
			entropys[i] = entropy;
		}
		return entropys;
	}
	
	/**
	 *  ��������������Ӳ����޸��������˽ṹ
	 */
	public void modifyNetworkByVirtualLink(){
		//ѡ�������ж�����С�Ķ���
		int nodeId = 0;
		int minDegree = Integer.MAX_VALUE;
		int[] degrees = new int[nodeSet.size()];		//��¼ÿ������Ķ���
		for(int i=0;i<adjacencyMat.getRowDimension();i++){
			int curDegree = 0;
			for(int j=0;j<adjacencyMat.getColumnDimension();j++){
				curDegree += adjacencyMat.get(i, j);
			}
			degrees[i] = curDegree;
			if(curDegree < minDegree){
				nodeId = i;
				minDegree = curDegree;
			}
		}
		//�ҵ�����С�ȶ����������������ƵĶ���
		Set<Integer> linkNode = new HashSet<Integer>();
		Set<Integer> unlinkNode = new HashSet<Integer>();
		for(int j=0;j<adjacencyMat.getColumnDimension();j++){
			if(nodeId == j) continue;
			if(adjacencyMat.get(nodeId,j) == 1){
				linkNode.add(j);
			}else{
				unlinkNode.add(j);
			}
		}
		//ѡ����һ���������ӵĵ�
		double[][] mat = adjacencyMat.getArray();
		double maxAvgSimilarity = Double.MIN_VALUE;
		int targetunLinkId = 0;
		for(int unLinkId : unlinkNode){
			double similaritySum = 0.0;
			for(int linkId:linkNode){
				similaritySum += calcSimilarity(mat[unLinkId],mat[linkId],degrees[unLinkId],degrees[linkId]);
			}
			if(((double)similaritySum/linkNode.size()) > maxAvgSimilarity){
				maxAvgSimilarity = (double)similaritySum/linkNode.size();
				targetunLinkId = unLinkId;
			}
		}
		
		//�������
		System.out.println("����������ߣ�"+nodeSet.get(nodeId).getName()+","+nodeSet.get(targetunLinkId).getName()+"��");
		adjacencyMat.set(nodeId, targetunLinkId, 1);
	}
	
	/**
	 * ��������������Salton Index���ƶ�
	 * @param s s����
	 * @param t t����
	 * @param sDegree
	 * @param tDegree
	 * @return
	 */
	public double calcSimilarity(double[] s,double[] t,int sDegree,int tDegree){
		double res = 0.0;
		double sum = 0.0;
		for(int i=0;i<s.length;i++){
			sum += s[i]*t[i];
		}
		if(sDegree*tDegree == 0){
			res = Double.MIN_VALUE;
		}else{
			res = sum/Math.sqrt(sDegree*tDegree);
		}
		return res;
	}
	
	/**
	 * ������ӡ�ɾ�����Ӳ����޸��������˽ṹ
	 * @param Entropys
	 */
	public void modifyNetworkByConnectionStrategy(double[] entropys,int maxLinkSize){
		//�ҳ�ÿ����������������С��
		for(int i:CommsOfNetwork.keySet()){
			Community comm = CommsOfNetwork.get(i);
			Set<Integer> nodeSet = comm.getNodeSet();
			if(nodeSet != null && nodeSet.size() != 0){
				int minEntropyId = 0;
				for(int nodeId:nodeSet){
					double entropy = entropys[nodeId];
					if(entropy < entropys[minEntropyId]){
						minEntropyId = nodeId;
					}
				}
				comm.setHubs(minEntropyId);
			}
			CommsOfNetwork.put(i, comm);
		}
		//������Ӻ�ɾ�����Ӳ����޸���������˽ṹ
		
		List<Edge> edgeList = sortInterLinkByEntropy(entropys);
		for(int n=0; n<SELECTED_SIZE && n<edgeList.size(); n++){
			Edge edge = edgeList.get(n);
			System.out.println("ѡ��ıߵ��أ�"+edge.getEntropy());
			Node maxEntrNodeOne = nodeSet.get(edge.getSourceId());
			Node maxEntrNodeAnother = nodeSet.get(edge.getDestinationId());
			//���ݱ�����Ϣ���ж������������Ƿ�����ͬһ���������������ͬһ��������ϵ����ӣ�������
			boolean isIdentical = maxEntrNodeOne.isIdenticalCommunity(maxEntrNodeAnother);
//			recordHumanLabeling(edge);
			if(!isIdentical){
				modifyAdjacencyMatrix(edge.getSourceId(), edge.getDestinationId(), 0,maxLinkSize);
//				
//				adjacencyMat.set(edge.getSourceId(), edge.getDestinationId(), 0);
//				adjacencyMat.set(edge.getDestinationId(), edge.getSourceId(), 0);
			}
			
			connetionStrategy(maxEntrNodeOne, maxEntrNodeAnother, isIdentical,maxLinkSize);
			disconnetionStrategy(maxEntrNodeOne, maxEntrNodeAnother, isIdentical,maxLinkSize);
		}
	}
	
	/**
	 * ������Ӳ���
	 * @param maxEntrNodeOne
	 * @param maxEntrNodeAnother
	 * @param isIdentical
	 */
	public void connetionStrategy(Node maxEntrNodeOne,Node maxEntrNodeAnother,boolean isIdentical,int maxLinkSize){
		//�������������ͬһ���������ҵ������������Ŧ��
		if(isIdentical){
			int hubsId = 0;
			if(maxEntrNodeOne.detectIsTrue()){
				hubsId = CommsOfNetwork.get(maxEntrNodeOne.getCommId()).getHubs();
			}else{
				hubsId = CommsOfNetwork.get(maxEntrNodeAnother.getCommId()).getHubs();
			}
			
			int nodeId = maxEntrNodeOne.getId();
			modifyAdjacencyMatrix(nodeId,hubsId,1,maxLinkSize);
			
			nodeId = maxEntrNodeAnother.getId();
			modifyAdjacencyMatrix(nodeId,hubsId,1,maxLinkSize);
		}else{
			int nodeId = maxEntrNodeOne.getId();
			int hubsId = CommsOfNetwork.get(maxEntrNodeOne.getCommId()).getHubs();
			modifyAdjacencyMatrix(nodeId,hubsId,1,maxLinkSize);
			
			nodeId = maxEntrNodeAnother.getId();
			hubsId = CommsOfNetwork.get(maxEntrNodeAnother.getCommId()).getHubs();
			modifyAdjacencyMatrix(nodeId,hubsId,1,maxLinkSize);
		}
	}
	
	/**
	 * �޸��ڽӾ���
	 * @param rowId
	 * @param colId
	 * @param value
	 */
	public void modifyAdjacencyMatrix(int rowId,int colId,double value,int maxLinkSize){
		if(humanLabeling.size() >= maxLinkSize){
			return;
		}
		if(rowId != colId && adjacencyMat.get(rowId, colId) != value){
			recordHumanLabeling(new Edge(rowId,colId,value==0.0?EdgeType.DISLINK:EdgeType.LINK));
			adjacencyMat.set(rowId, colId, value);
			adjacencyMat.set(colId, rowId, value);
		}
	}
	
	/**
	 * ɾ�����Ӳ���
	 * @param maxEntrNodeOne
	 * @param maxEntrNodeAnother
	 * @param isIdentical
	 */
	public void disconnetionStrategy(Node maxEntrNodeOne,Node maxEntrNodeAnother,boolean isIdentical,int maxLinkSize){
		if(isIdentical){
			int commId = 0;					//�ҵ�Ҫ�ϵ����ӵ�����
			if(!maxEntrNodeOne.detectIsTrue()){
				commId = maxEntrNodeOne.getCommId();
			}else{
				commId = maxEntrNodeAnother.getCommId();
			}
			int nodeId = maxEntrNodeOne.getId();
			for(int n=0,colNum=adjacencyMat.getColumnDimension();n<colNum;n++){
				if(adjacencyMat.get(nodeId, n) == 1 && nodeSet.get(n).getCommId() == commId){
					modifyAdjacencyMatrix(nodeId, n, 0,maxLinkSize);
				}
			}
			nodeId = maxEntrNodeAnother.getId();
			for(int n=0,colNum=adjacencyMat.getColumnDimension();n<colNum;n++){
				if(adjacencyMat.get(nodeId, n) == 1 && nodeSet.get(n).getCommId() == commId){
					modifyAdjacencyMatrix(nodeId, n, 0,maxLinkSize);
				}
			}
		}
//		else{
//			int commId = maxEntrNodeAnother.getCommId();
//			int nodeId = maxEntrNodeOne.getId();
//			for(int n=0,colNum=adjacencyMat.getColumnDimension();n<colNum;n++){
//				if(adjacencyMat.get(nodeId, n) == 1 && nodeSet.get(n).getCommId() == commId){
//					adjacencyMat.set(nodeId, n, 0);
//					adjacencyMat.set(n, nodeId, 0);
//				}
//			}
//			commId = maxEntrNodeOne.getCommId();
//			nodeId = maxEntrNodeAnother.getId();
//			for(int n=0,colNum=adjacencyMat.getColumnDimension();n<colNum;n++){
//				if(adjacencyMat.get(nodeId, n) == 1 && nodeSet.get(n).getCommId() == commId){
//					adjacencyMat.set(nodeId, n, 0);
//					adjacencyMat.set(n, nodeId, 0);
//				}
//			}
//		}
	}
	
	
	/**
	 * ��¼��ӻ�ɾ���ı�
	 * @param edge
	 */
	public void recordHumanLabeling(Edge edge){
		for(Edge e:humanLabeling){
			if(edge.equals(e)){
				if(e.getType().getIndex() != edge.getType().getIndex()){
					humanLabeling.remove(e);
				}
				return;
			}
		}
		humanLabeling.add(edge);
	}
	
	/**
	 * ��ȡ����������ӣ����������ӵ��ؽ�������
	 * @param entropys
	 * @return
	 */
	public List<Edge> sortInterLinkByEntropy(double[] entropys){
		List<Edge> list = new ArrayList<Edge>();
		for(int i=0;i<K-1;i++){
			for(int j=i+1;j<K;j++){
				CommsOfNetwork.get(i).getNodeSet();
				sort(CommsOfNetwork.get(i).getNodeSet(),CommsOfNetwork.get(j).getNodeSet(),entropys,list);
			}
		}
		Collections.sort(list);
		return list;
	}
	
	/**
	 * ��ȡ����������ӣ����������ӵ���
	 * @param oneSet
	 * @param anotherSet
	 * @param entropys
	 * @param list
	 */
	public void sort(Set<Integer> oneSet,Set<Integer> anotherSet,double[] entropys,List<Edge> list){
		if(oneSet == null || anotherSet == null || oneSet.size() == 0 || anotherSet.size() == 0){
			return;
		}
		for(int one:oneSet){
			for(int another:anotherSet){
				if(adjacencyMat.get(one,another) == 1){
					double entropy = entropys[one]+entropys[another];
					list.add(new Edge(one, another, entropy));
				}
			}
		}
	}
	
	/**
	 * ���ݱ�ǩ��ȡ����ID
	 * @param label
	 * @return
	 */
	public int getCommIdByLabel(String label){
		int j=0;
		for(;j<communityLabels.length;j++){
			if(communityLabels[j].equals(label)){
				break;
			}
		}
		if(j == communityLabels.length){
			System.out.println("���ִ���"+label);
		}
		return j;
	}
	
	
	/**
	 * ��ȡ�������������
	 * @param arr ��ʾX����
	 * @return
	 */
	public boolean getConmunityOfNode(double[][] arr){
		boolean modifyFlag = false;
		//��¼���������ĵ�
		Map<Integer,Set<Integer>> nodesetOfCom = new HashMap<Integer,Set<Integer>>();
		for(int i=0;i<arr.length;i++){
			int max = 0;
			for(int j=1;j<arr[i].length;j++){
				if(arr[i][j]>arr[i][max]){
					max = j;
				}
			}
			if(nodesetOfCom.get(max) == null){
				nodesetOfCom.put(max, new HashSet<Integer>());
			}
			nodesetOfCom.get(max).add(i);
		}
		
		//���ݱ�����Ϣȷ������������
		int[][] numOfComm = new int[K][K]; 	//ÿ������������ʵ�����ڸ�������������
		List<Integer> flag = new ArrayList<Integer>();
		for(int i=0;i<K;i++){
			flag.add(i, -1);
		}
		for(int commId:nodesetOfCom.keySet()){
			for(int nodeId:nodesetOfCom.get(commId)){
				Node node = nodeSet.get(nodeId);
				int realCommId = getCommIdByLabel(node.getRealLabel());
				numOfComm[commId][realCommId]++;
			}
		}
		
		//ȷ����㼯ʵ�ʶ�Ӧ������������������ڵ㼯���ֵ�ͬһ�����ĳ�ͻ����
		Set<Integer> conflict = new HashSet<Integer>();
		for(int i=0;i<K;i++){
			int maxIndex = 0;
			for(int j=1;j<K;j++){
				if(numOfComm[i][j] > numOfComm[i][maxIndex]){
					maxIndex = j;
				}
			}
			if(flag.get(maxIndex) == -1){
				flag.set(maxIndex, i);
			}else{
				if(numOfComm[i][maxIndex] > numOfComm[flag.get(maxIndex)][maxIndex]){
					conflict.add(flag.get(maxIndex));
					flag.set(maxIndex, i);
				}else{
					conflict.add(i);
				}
			}
		}
		for(int k : conflict){
			for(int n =0;n<K;n++){
				if(flag.get(n) == -1){
					flag.set(n, k);
					break;
				}
			}
		}
		for(int m =0;m<flag.size();m++){
			CommsOfNetwork.get(m).setNodeSet(nodesetOfCom.get(flag.get(m)));
		}
		
		//����ÿ���ڵ�����������Ϣ
		for(int commId:CommsOfNetwork.keySet()){
			Set<Integer> nodeIdSet = CommsOfNetwork.get(commId).getNodeSet();
			if(nodeIdSet == null || nodeIdSet.size() == 0){
				continue;
			}
			for(int nodeId:nodeIdSet){
				int commIndex = nodeSet.get(nodeId).getCommId();		//���ԭ�����������
				if(commIndex != commId){
					nodeSet.get(nodeId).setCommId(commId);
					nodeSet.get(nodeId).setDetectLabel(communityLabels[commId]);
					modifyFlag = true;
				}
			}
		}
		
		
		return modifyFlag;
	}
	
	
	/**
	 * �������ı�׼����Ϣֵ
	 */
	public double calculateNMI(){
		int[][] nij = new int[K][K];
		int[] groundTruth = new int[K];
		int[] results = new int[K];
		int trueDetectSize = 0;		//������ȷ�Ķ�����
		//ͳ�ƽ����Ϣ
		for(Node node:nodeSet.values()){
			int tCommId = getCommIdByLabel(node.getRealLabel());
			int dCommId = getCommIdByLabel(node.getDetectLabel());
			nij[tCommId][dCommId]++;
			groundTruth[tCommId]++;
			results[dCommId]++;
			if(node.detectIsTrue()){
				trueDetectSize++;
			}
		}
		System.out.println("��ȷ���ֵĶ�����Ϊ��"+trueDetectSize);
		
		//�����׼����Ϣ
		double nmi = 0;
		double topPart = 0.0;
		double buttomLeftPart = 0.0;
		double buttomRightPart = 0.0;
		for(int i=0;i<K;i++){
			for(int j=0;j<K;j++){
				if(groundTruth[i]*results[j]*nij[i][j] == 0){
					continue;
				}
				double temp = (double)(nodeSet.size()*nij[i][j])/(groundTruth[i]*results[j]);
				topPart +=nij[i][j]*Math.log(temp);
			}
			if(groundTruth[i]!=0){
				buttomLeftPart += groundTruth[i]*Math.log((double)groundTruth[i]/nodeSet.size());
			}
			if(results[i]!=0){
				buttomRightPart += results[i]*Math.log((double)results[i]/nodeSet.size());
			}
		}
		if(buttomLeftPart*buttomRightPart == 0){
			System.out.println("����ı�׼����ϢֵΪ��"+0);
		}else{
			nmi = topPart/Math.sqrt(buttomLeftPart*buttomRightPart);
			System.out.println("����ı�׼����ϢֵΪ��"+nmi+",��ӵ�������Ϊ��"+humanLabeling.size());
		}
		return nmi;
	}
	
	/**
	 * ��ӵ�
	 * @param node
	 */
	public void addNode(Node node){
		if(!nodeSet.keySet().contains(node.getId())){
			nodeSet.put(node.getId(), node);
		}
	}
	public int getNodeSize() {
		return nodeSet.size();
	}

	public Matrix getAdjacencyMat() {
		return adjacencyMat;
	}

	public void setAdjacencyMat(Matrix adjacencyMat) {
		this.adjacencyMat = adjacencyMat;
	}
	
	public void showDetectionResult(boolean isNode){
		if(isNode){
			for(int commId:CommsOfNetwork.keySet()){
				System.out.println("��"+(commId+1)+"�������ĵ㼯��");
				Set<Integer> nodeIdSet = CommsOfNetwork.get(commId).getNodeSet();
				for(int nodeId:nodeIdSet){
					Node node = nodeSet.get(nodeId);
					System.out.println(node.toString());
				}
				System.out.println();
			}
		}else{
			System.out.println("�ܹ���ӻ�ɾ����������"+humanLabeling.size());
			for(Edge e:humanLabeling){
				System.out.println(e.toString());
			}
		}
	}
	
	/**
	 * ��ʾ������Ϣ
	 */
	public void showNetWork(){
		System.out.println("��������"+nodeSet.size());
//		System.out.println("�����еĶ�����Ϣ��");
//		for(Node n:nodeSet.values()){
//			System.out.println(n.toString());
//		}
//		System.out.println("�����ڽӾ�����Ϣ��");
//		adjacencyMat.print(1, 0);
	}
}
