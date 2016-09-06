package cn.cas.cigit.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.cas.cigit.model.Edge;
import cn.cas.cigit.model.Node;

/**
 * �����ļ���������
 * 
 * @author qqx
 * 
 */
public abstract class DatasetParse {
	protected List<Edge> edgeSet = new ArrayList<Edge>(); 	//�洢�߼�
	protected List<Node> nodeSet = new ArrayList<Node>();	//�洢�㼯
	protected Set<String> labels = new HashSet<String>();	//�洢������ǩ��
	
	/**
	 * ���������ļ�
	 */
	public abstract void parseDatasetFile();
	
	/**
	 * ��ȡ�ļ��ľ���·��
	 * @param relativePath ���·��
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
