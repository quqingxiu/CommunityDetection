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
 * 网络实例类
 * @author qqx
 *
 */
public class Network {
	/**
	 * 网络中社区的个数
	 */
	private int K = 2;
	/**
	 * 选择人为标识的数量
	 */
	private int SELECTED_SIZE = 1;
	/**
	 * 网络中社区的名称
	 */
	private String[] communityLabels = null;
	/**
	 * 定义邻接矩阵
	 */
	private Matrix adjacencyMat = null;
	/**
	 * 网络点集
	 */
	private Map<Integer,Node> nodeSet = new HashMap<Integer,Node>();
	/**
	 * 记录网络中的社区
	 */
	private Map<Integer,Community> CommsOfNetwork = new HashMap<Integer, Community>();
	/**
	 * 记录添加或删除的链接
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
	 * 计算节点的熵
	 * @param arr 表示X矩阵
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
				System.out.println("出现NaN, "+CollectionUtil.toString(arr[i])+", sum: "+rowSum[i]);
			}
			entropys[i] = entropy;
		}
		return entropys;
	}
	
	/**
	 *  根据添加虚拟链接策略修改网络拓扑结构
	 */
	public void modifyNetworkByVirtualLink(){
		//选择网络中度数最小的顶点
		int nodeId = 0;
		int minDegree = Integer.MAX_VALUE;
		int[] degrees = new int[nodeSet.size()];		//记录每个顶点的度数
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
		//找到与最小度顶点相连顶点最相似的顶点
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
		//选出另一个接受连接的点
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
		
		//添加连边
		System.out.println("添加虚拟连边（"+nodeSet.get(nodeId).getName()+","+nodeSet.get(targetunLinkId).getName()+"）");
		adjacencyMat.set(nodeId, targetunLinkId, 1);
	}
	
	/**
	 * 计算两个向量的Salton Index相似度
	 * @param s s向量
	 * @param t t向量
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
	 * 根据添加、删除链接策略修改网络拓扑结构
	 * @param Entropys
	 */
	public void modifyNetworkByConnectionStrategy(double[] entropys,int maxLinkSize,int perSelectedSize){
		//找出每个社区中熵最大和最小点
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
		
		//根据添加和删除链接策略修改网络的拓扑结构
		List<Edge> edgeList = sortInterLinkByEntropy(entropys);
		for(int n=0; n<perSelectedSize && n<edgeList.size(); n++){
			Edge edge = edgeList.get(n);
//			System.out.println("选择的边的熵："+edge.getEntropy());
			Node maxEntrNodeOne = nodeSet.get(edge.getSourceId());
			Node maxEntrNodeAnother = nodeSet.get(edge.getDestinationId());
			//根据背景信息，判断两个熵最大点是否属于同一社区。如果不属于同一社区，则断掉连接，否则保留
			boolean isIdentical = maxEntrNodeOne.isIdenticalCommunity(maxEntrNodeAnother);
//			recordHumanLabeling(edge);
			if(!isIdentical){
				modifyAdjacencyMatrix(edge.getSourceId(), edge.getDestinationId(), 0,maxLinkSize);
			}
			
			connetionStrategy(maxEntrNodeOne, maxEntrNodeAnother, isIdentical,maxLinkSize);
			disconnetionStrategy(maxEntrNodeOne, maxEntrNodeAnother, isIdentical,maxLinkSize);
		}
	}
	
	/**
	 * 添加链接策略
	 * @param maxEntrNodeOne
	 * @param maxEntrNodeAnother
	 * @param isIdentical
	 */
	public void connetionStrategy(Node maxEntrNodeOne,Node maxEntrNodeAnother,boolean isIdentical,int maxLinkSize){
		//如果两个点属于同一社区，则找到正真的社区枢纽点
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
	 * 修改邻接矩阵
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
	 * 删除链接策略
	 * @param maxEntrNodeOne
	 * @param maxEntrNodeAnother
	 * @param isIdentical
	 */
	public void disconnetionStrategy(Node maxEntrNodeOne,Node maxEntrNodeAnother,boolean isIdentical,int maxLinkSize){
		if(isIdentical){
			int commId = 0;					//找到要断掉连接的社区
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
	 * 记录添加或删除的边
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
	 * 获取社区间的连接，并根据链接的熵进行排序
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
	 * 获取社区间的连接，并计算连接的熵
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
	 * 根据标签获取社区ID
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
			System.out.println("出现错误："+label);
		}
		return j;
	}
	
	
	/**
	 * 获取结点所属的社区
	 * @param arr 表示X矩阵
	 * @return
	 */
	public boolean getConmunityOfNode(double[][] arr){
		boolean modifyFlag = false;
		//记录社区包含的点
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
		
		//根据背景信息确定社区的名称
		int[][] numOfComm = new int[K][K]; 	//每个集合中属于实际属于各个社区的数量
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
		
		//确定结点集实际对应的社区，并解决两个节点集划分到同一社区的冲突问题
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
		
		//更新每个节点所属社区信息
		for(int commId:CommsOfNetwork.keySet()){
			Set<Integer> nodeIdSet = CommsOfNetwork.get(commId).getNodeSet();
			if(nodeIdSet == null || nodeIdSet.size() == 0){
				continue;
			}
			for(int nodeId:nodeIdSet){
				int commIndex = nodeSet.get(nodeId).getCommId();		//结点原所属社区编号
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
	 * 计算结果的标准互信息值
	 */
	public double calculateNMI(){
		int[][] nij = new int[K][K];
		int[] groundTruth = new int[K];
		int[] results = new int[K];
		int trueDetectSize = 0;		//划分正确的顶点数
		//统计结果信息
		for(Node node:nodeSet.values()){
			int rCommId = getCommIdByLabel(node.getRealLabel());
			int dCommId = getCommIdByLabel(node.getDetectLabel());
			nij[rCommId][dCommId]++;
			groundTruth[rCommId]++;
			results[dCommId]++;
			if(node.detectIsTrue()){
				trueDetectSize++;
			}
		}
		System.out.println("正确划分的顶点数为："+trueDetectSize);
		
		//计算标准互信息
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
			System.out.println("结果的标准互信息值为："+0);
		}else{
			nmi = topPart/Math.sqrt(buttomLeftPart*buttomRightPart);
			System.out.println("结果的标准互信息值为："+nmi+",添加的连接数为："+humanLabeling.size());
		}
		return nmi;
	}
	
	/**
	 * 添加点
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
	
	public int getCommunitySize() {
		return K;
	}

	public void showDetectionResult(boolean isNode){
		if(isNode){
			for(int commId:CommsOfNetwork.keySet()){
				System.out.println("第"+(commId+1)+"个社区的点集：");
				Set<Integer> nodeIdSet = CommsOfNetwork.get(commId).getNodeSet();
				for(int nodeId:nodeIdSet){
					Node node = nodeSet.get(nodeId);
					System.out.println(node.toString());
				}
				System.out.println();
			}
		}else{
			System.out.println("总共添加或删除边数量："+humanLabeling.size());
			for(Edge e:humanLabeling){
				System.out.println(e.toString());
			}
		}
	}
	
	/**
	 * 显示网络信息
	 */
	public void showNetWork(){
		System.out.println("顶点数："+nodeSet.size());
//		System.out.println("网络中的顶点信息：");
//		for(Node n:nodeSet.values()){
//			System.out.println(n.toString());
//		}
//		System.out.println("网络邻接矩阵信息：");
//		adjacencyMat.print(1, 0);
	}
}
