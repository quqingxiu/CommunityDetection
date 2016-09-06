package cn.cas.cigit.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Node;

/**
 * 数据文件解析虚类
 * 
 * @author qqx
 * 
 */
public abstract class DatasetParse {
	protected List<Edge> edgeSet = new ArrayList<Edge>(); 	//存储边集
	protected List<Node> nodeSet = new ArrayList<Node>();	//存储点集
	protected Set<String> labels = new HashSet<String>();	//存储社区标签集
	
	/**
	 * 解析数据文件
	 */
	public abstract void parseDatasetFile();
	
	/**
	 * 获取文件的绝对路径
	 * @param relativePath 相对路径
	 * @return
	 */
	public static String getFileAbsolutePath(String relativePath){
		String rootPath = System.getProperty("user.dir");
		if(!relativePath.startsWith("/")){
			return rootPath+File.separator+relativePath;
		}else{
			return rootPath+relativePath;
		}
	}
	
	public List<Edge> getEdgeSet() {
		return edgeSet;
	}
	
	public List<Node> getNodeSet() {
		return nodeSet;
	}
	
	public Set<String> getLabels() {
		return labels;
	}
}
