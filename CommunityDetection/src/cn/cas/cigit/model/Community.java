package cn.cas.cigit.model;

import java.util.Set;
import java.lang.Comparable;

/**
 * 社区实例类
 * @author qqx
 *
 */
public class Community implements Comparable<Community> {
	/**
	 * 社区标签
	 */
	private Integer communityLabel;
	/**
	 * 中心点
	 */
	private Integer hubs;
	/**
	 * 社区点集
	 */
	private Set<Integer> nodeSet = null;

	/**
	 * 向社区中添加点
	 * @param node
	 */
	public void addNode(int nodeId){
		if(!nodeSet.contains(nodeId)){
			nodeSet.add(nodeId);
		}
	}
	
	public Community(Integer communityLabel) {
		super();
		this.communityLabel = communityLabel;
	}
	public Set<Integer> getNodeSet() {
		return nodeSet;
	}
	
	public void setNodeSet(Set<Integer> nodeSet) {
		this.nodeSet = nodeSet;
	}
	
	public Integer getCommunityLabel() {
		return communityLabel;
	}
	public void setCommunityLabel(Integer communityLabel) {
		this.communityLabel = communityLabel;
	}
	
	public Integer getHubs() {
		return hubs;
	}
	public void setHubs(Integer hubs) {
		this.hubs = hubs;
	}

	@Override
	public int compareTo(Community com) {
		return ((Integer)com.getNodeSet().size()).compareTo(this.getNodeSet().size());
	}


}
