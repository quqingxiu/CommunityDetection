package cn.cas.cigit.test;

public class Name {
	private double[] arr ;
	private String id;
	private String name;
	public Name(double[] aa){
		this.arr = aa;
	}
	
	public Name(String name,String id){
		this.name = name;
		this.id = id;
	}
	public void change(double a){
		for(int i=0;i<arr.length;i++){
			arr[i] = a;
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void show(){
		for(int i=0;i<arr.length;i++){
			System.out.print(arr[i]+" ");
		}
	}
}
