package cn.cas.cigit.model;

/**
 * 结点实例类
 * @author qqx
 *
 */
public class Node {
	/**
	 * 结点Id
	 */
	private Integer id;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 实际类型
	 */
	private String realLabel;
	/**
	 * 划分后类型
	 */
	private String detectLabel;
	/**
	 * 所属社区Id
	 */
	private Integer commId=-1;
	
	/**
	 * 判断结点是否划分正确
	 * @return
	 */
	public boolean detectIsTrue(){
		if(realLabel != null){
			return realLabel.equals(detectLabel);
		}
		return false;
	}
	/**
	 * 判断两个点是否属于同一社区
	 * @param n
	 * @return
	 */
	public boolean isIdenticalCommunity(Node n){
		return realLabel.equals(n.getRealLabel());
	}
	
	@Override
	public String toString() {
		return "id:" + id + ", name:" + name
				+ ", realLabel:" + realLabel + ", detectLabel:"+detectLabel+", commId:"+commId;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCommId() {
		return commId;
	}
	public void setCommId(Integer commId) {
		this.commId = commId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRealLabel() {
		return realLabel;
	}
	public void setRealLabel(String realLabel) {
		this.realLabel = realLabel;
	}
	public String getDetectLabel() {
		return detectLabel;
	}
	public void setDetectLabel(String detectLabel) {
		this.detectLabel = detectLabel;
	}
}
