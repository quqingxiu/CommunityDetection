package cn.cas.cigit.parse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.cas.cigit.data.FileUtils;
import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Node;

/**
 * amazon���ݼ�������
 * @author qqx
 *
 */
public class AmazonDatasetParse extends DatasetParse {

	@Override
	public void parseDatasetFile() {
		String ungraphPath = "data/amazon/formal-amazon.ungraph.txt";
		String cmtyPath = "data/amazon/formal-amazon.top705.cmty.txt";
		
		Map<String,Integer> nodeToId = new HashMap<String,Integer>();
		try {
			//����������Ϣ����
			List<String> rawCommunityData = FileUtils.readFileByLine(new File(getFileAbsolutePath(cmtyPath)));
			int[] sizeOfComm = new int[rawCommunityData.size()]; 		//��¼ÿ�������Ľڵ�����
			int commId = 0;
			int nId = 0;
			for(String line : rawCommunityData){
				String[] nodeIds = line.trim().split("\t");
				for(String nodeId:nodeIds){
					if(nodeToId.keySet().contains(nodeId)){		//�����ص��ڵ㣬������ڵ����ٵ�����
						for(Node node:nodeSet){
							if(nodeId.equals(node.getName()) && sizeOfComm[Integer.valueOf(node.getRealLabel())] > sizeOfComm[commId]){
									node.setCommId(commId);
									sizeOfComm[commId]++;
									sizeOfComm[Integer.valueOf(node.getRealLabel())]--;
							}
						}
						continue;
					}else{
						Node node = new Node();
						node.setRealLabel(commId+"");
						node.setId(nId);
						node.setName(nodeId);
						nodeSet.add(node);
						nodeToId.put(nodeId, nId++);
						sizeOfComm[commId]++;
					}
				}
				labels.add(commId+"");
				commId++;
			}
			
			//��������ͼ����
			List<String> rawUngraphData = FileUtils.readFileByLine(new File(getFileAbsolutePath(ungraphPath)));
			for(String line : rawUngraphData){
				String[] nodeIds = line.trim().split("\t");
				if(nodeToId.get(nodeIds[0]) == null || nodeToId.get(nodeIds[1]) == null){
					System.out.println("���ݲ���ȷ��"+line);
					continue;
				}
				Edge edge = new Edge();
				edge.setSourceId(nodeToId.get(nodeIds[0]));
				edge.setDestinationId(nodeToId.get(nodeIds[1]));
				edgeSet.add(edge);
			}
			
			for(int i=0;i<sizeOfComm.length;i++){
				System.out.println(i+" , "+sizeOfComm[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
