package cn.cas.cigit.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Node;

/**
 * gml格式数据集文件解析类
 * @author qqx
 *
 */
public class GMLDatasetParse extends DatasetParse {
	private String gmlFilePath;
	
	public GMLDatasetParse(String gmlFilePath) {
		super();
		this.gmlFilePath = gmlFilePath;
	}

	@Override
	public void parseDatasetFile() {
		BufferedReader buffer = null;
		try {
			File file = new File(getFileAbsolutePath(gmlFilePath));
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));
			buffer = new BufferedReader(read);
			String line;
			String type = null;
			Node node = null;
			Edge edge = null;
			int directed = 0;		//标识结点id是从1还是0开始
			while ((line = buffer.readLine()) != null) {
				line = line.trim();
				String x[] = line.split(" ");
				if(x[0].equals("node")){		//解析生成点实例
					node = new Node();
					type = "node";
					continue;
				}else if(x[0].equals("edge")){	//解析生成边实例
					edge = new Edge();
					type = "edge";
					continue;
				}else if(x[0].equals("]")){
					if("node".equals(type)){
						nodeSet.add(node);
					}else if("edge".equals(type)){
						edgeSet.add(edge);
					}
					type = null;
					continue;
				}else if(x[0].equals("directed")){		//结点id起始值
					directed = Integer.parseInt(x[1]);
					continue;
				}
				
				if("node".equals(type)){
					if("id".equals(x[0])){
						node.setId(Integer.parseInt(x[1])-directed);
					}else if("label".equals(x[0])){
						node.setName(x[1]);
					}else if("value".equals(x[0])){
						node.setRealLabel(x[1]);
						labels.add(x[1]);
					}
				}else if("edge".equals(type)){
					if("source".equals(x[0])){
						edge.setSourceId(Integer.parseInt(x[1])-directed);
					}else if("target".equals(x[0])){
						edge.setDestinationId(Integer.parseInt(x[1])-directed);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(buffer != null){
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
