package cn.cas.cigit.test;

import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Node;

public class DeadCode {
	
//	for(int i=0;i<K-1;i++){
//		for(int j=i+1;j<K;j++){
//			if(humanLabeling.size() >= linkSize){
//				return;
//			}
//			
//			//�ҵ���������������������
//			int[] maxEntropyLink = getMaxEntropyLink(CommsOfNetwork.get(i).getNodeSet(), CommsOfNetwork.get(j).getNodeSet(), entropys);
//			if(maxEntropyLink[0] == 0 && maxEntropyLink[1] == 0){
//				continue;
//			}
//			Node maxEntrNodeOne = nodeSet.get(maxEntropyLink[0]);
//			Node maxEntrNodeAnother = nodeSet.get(maxEntropyLink[1]);
//			//���ݱ�����Ϣ���ж������������Ƿ�����ͬһ���������������ͬһ��������ϵ����ӣ�������
//			boolean isIdentical = maxEntrNodeOne.isIdenticalCommunity(maxEntrNodeAnother);
//			recordHumanLabeling(new Edge(maxEntropyLink[0], maxEntropyLink[1], EdgeType.DISLINK));
//			if(!isIdentical){
//				adjacencyMat.set(maxEntropyLink[0], maxEntropyLink[1], 0);
//				adjacencyMat.set(maxEntropyLink[1], maxEntropyLink[0], 0);
//			}
//			
//			connetionStrategy(maxEntrNodeOne, maxEntrNodeAnother, isIdentical);
//			disconnetionStrategy(maxEntrNodeOne, maxEntrNodeAnother, isIdentical);
//		}
//	}


}
