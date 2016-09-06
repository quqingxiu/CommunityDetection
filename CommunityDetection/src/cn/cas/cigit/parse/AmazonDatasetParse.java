package cn.cas.cigit.parse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.cas.cigit.data.FileUtils;
import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Node;

/**
 * amazon数据集解析类
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
			//解析社区信息数据
			List<String> rawCommunityData = FileUtils.readFileByLine(new File(getFileAbsolutePath(cmtyPath)));
			int commId = 0;
			int nId = 0;
			for(String line : rawCommunityData){
				if(line.startsWith("#")){
					continue;
				}
				String[] nodeIds = line.trim().split("\t");
				for(String nodeId:nodeIds){
					if(nodeToId.keySet().contains(nodeId)){
						
						continue;
					}else{
						Node node = new Node();
						node.setRealLabel(commId+"");
						node.setId(nId);
						node.setName(nodeId);
						nodeSet.add(node);
						nodeToId.put(nodeId, nId++);
					}
				}
				labels.add(commId+"");
				commId++;
			}
			
			//解析无向图数据
			List<String> rawUngraphData = FileUtils.readFileByLine(new File(getFileAbsolutePath(ungraphPath)));
			for(String line : rawUngraphData){
				if(line.startsWith("#")){
					continue;
				}
				String[] nodeIds = line.trim().split("\t");
				if(nodeToId.get(nodeIds[0]) == null || nodeToId.get(nodeIds[1]) == null){
					System.out.println("数据不正确："+line);
					continue;
				}
				Edge edge = new Edge();
				edge.setSourceId(nodeToId.get(nodeIds[0]));
				edge.setDestinationId(nodeToId.get(nodeIds[1]));
				edgeSet.add(edge);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
