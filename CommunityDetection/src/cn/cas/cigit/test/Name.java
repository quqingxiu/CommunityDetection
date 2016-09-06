package cn.cas.cigit.test;

public class Name {
	double[] arr ;
	public Name(double[] aa){
		this.arr = aa;
	}
	public void change(double a){
		for(int i=0;i<arr.length;i++){
			arr[i] = a;
		}
	}
	
	public void show(){
		for(int i=0;i<arr.length;i++){
			System.out.print(arr[i]+" ");
		}
	}
}
