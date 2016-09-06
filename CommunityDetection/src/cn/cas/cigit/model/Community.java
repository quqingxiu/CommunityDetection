package cn.cas.cigit.model;

import java.util.Set;
import java.lang.Comparable;

/**
 * ����ʵ����
 * @author qqx
 *
 */
public class Community implements Comparable<Community> {
	/**
	 * ������ǩ
	 */
	private Integer communityLabel;
	/**
	 * ���ĵ�
	 */
	private Integer hubs;
	/**
	 * �����㼯
	 */
	private Set<Integer> nodeSet = null;

	/**
	 * ����������ӵ�
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
