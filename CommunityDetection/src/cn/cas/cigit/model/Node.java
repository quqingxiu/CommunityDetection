package cn.cas.cigit.model;

/**
 * ���ʵ����
 * @author qqx
 *
 */
public class Node {
	/**
	 * ���Id
	 */
	private Integer id;
	/**
	 * ����
	 */
	private String name;
	/**
	 * ʵ������
	 */
	private String realLabel;
	/**
	 * ���ֺ�����
	 */
	private String detectLabel;
	/**
	 * ��������Id
	 */
	private Integer commId=-1;
	
	/**
	 * �жϽ���Ƿ񻮷���ȷ
	 * @return
	 */
	public boolean detectIsTrue(){
		if(realLabel != null){
			return realLabel.equals(detectLabel);
		}
		return false;
	}
	/**
	 * �ж��������Ƿ�����ͬһ����
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
