package cn.cas.cigit.model;

/**
 * ��ʵ����
 * @author qqx
 *
 */
public class Edge implements Comparable<Edge> {
	/**
	 * Դ��Id
	 */
	private int sourceId;
	/**
	 * Ŀ�ĵ�Id
	 */
	private int destinationId;
	/**
	 * �ߵ����ͣ���ӻ�Ͽ�
	 */
	private EdgeType type;
	/**
	 * �ߵ���
	 */
	private Double entropy;
	
	public Edge(int sourceId, int destinationId, EdgeType type) {
		super();
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.type = type;
	}
	
	public Edge(int sourceId, int destinationId, Double entropy) {
		super();
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.entropy = entropy;
		this.type = EdgeType.LINK;
	}



	public Edge() {
		this.type = EdgeType.LINK;
	}
	
	public String toString(){
		return "("+sourceId+","+destinationId+"),����:"+type.getName()+",�أ�"+entropy;
	}
	
	/**
	 * �ж��������Ƿ���ͬ
	 * @param e
	 * @return
	 */
	public boolean equals(Edge e){
		return (this.sourceId==e.sourceId && this.destinationId == e.destinationId) 
				|| (this.sourceId==e.destinationId && this.destinationId == e.sourceId);
	}

	public int getSourceId() {
		return sourceId;
	}

	public int getDestinationId() {
		return destinationId;
	}

	public EdgeType getType() {
		return type;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public void setType(EdgeType type) {
		this.type = type;
	}

	@Override
	public int compareTo(Edge e) {
		return e.getEntropy().compareTo(this.getEntropy());
	}

	public Double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}
}

enum EdgeType{
	LINK("��ӱ�",0),DISLINK("�Ͽ���",1);
	private String name;
	private int index;
	
	private EdgeType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
}

