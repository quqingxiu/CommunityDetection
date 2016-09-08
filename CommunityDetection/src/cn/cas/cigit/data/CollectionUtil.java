package cn.cas.cigit.data;

import java.util.Set;

public class CollectionUtil {
	public static void showArr(double[][] a){
		for(int i=0;i<a.length;i++){
			for(int j=0;j<a[i].length;j++){
				System.out.print(a[i][j] +" ");
			}
			System.out.println();
		}
	}
	
	public static void showArr(int[][] a){
		for(int i=0;i<a.length;i++){
			for(int j=0;j<a[i].length;j++){
				System.out.print(a[i][j] +" ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws Exception {
		double[][] one = {{1,1,1},{2,2,2},{3,3,3}};
		double[][] one1 = {{4,4,4},{5,5,5,5}};
		double[][] another = {{1,1},{2,2},{3,3}};
//		double[][] arr = horizontalMerge2DArray(one,another);
		double[][] arr = verticalMerge2DArray(one, one1);
		showArr(arr);
	}
	
	/**
	 * 水平合并两个二维数组
	 * @param oneArray
	 * @param anotherArray
	 * @return
	 * @throws Exception 
	 */
	public static double[][] horizontalMerge2DArray(double[][] oneArray,double[][] anotherArray) throws Exception{
		if(oneArray.length != anotherArray.length){
			throw new Exception("水平合并两个数组要求行相等!");
		}
		int lenofOneArray = oneArray[0].length;
		int lenofAnotherArray = anotherArray[0].length;
		double[][] res= new double[oneArray.length][lenofOneArray + lenofAnotherArray];
		for(int i=0,len=oneArray.length;i<len;i++){
			System.arraycopy(oneArray[i], 0, res[i], 0, lenofOneArray);
			System.arraycopy(anotherArray[i], 0, res[i], lenofOneArray, lenofAnotherArray);
		}
		return res;
	}
	
	/**
	 * 垂直合并两个二维数组
	 * @param oneArray
	 * @param anotherArray
	 * @return
	 * @throws Exception 
	 */
	public static double[][] verticalMerge2DArray(double[][] oneArray,double[][] anotherArray) throws Exception{
		if(oneArray[0].length != anotherArray[0].length){
			throw new Exception("水平合并两个数组要求行相等!");
		}
		int lenofOneArray = oneArray.length;
		int lenofAnotherArray = anotherArray.length;
		double[][] res= new double[lenofOneArray + lenofAnotherArray][oneArray[0].length];
		for(int i=0,len=lenofOneArray + lenofAnotherArray;i<len;i++){
			if(i<lenofOneArray){
				System.arraycopy(oneArray[i], 0, res[i], 0, oneArray[0].length);
			}else{
				System.arraycopy(anotherArray[i-lenofOneArray], 0, res[i], 0, anotherArray[0].length);
			}
		}
		return res;
	}
	
	
	/**
	 * 数组转换为字符
	 * @param arr
	 * @return
	 */
	public static String toString(double[] arr){
		String res = "[";
		for(Double d:arr){
			res += d+",";
		}
		res = res.substring(0, res.length()-1);
		res += "]";
		return res;
	}
	
	/**
	 * 数组转换为字符
	 * @param arr
	 * @return
	 */
	public static String toString(int[] arr){
		String res = "[";
		for(int d:arr){
			res += d+",";
		}
		res = res.substring(0, res.length()-1);
		res += "]";
		return res;
	}
	
	/**
	 * 数组转换为字符
	 * @param arr
	 * @return
	 */
	public static String toString(Set<String> arr){
		String res = "(";
		for(String d:arr){
			res += d+",";
		}
		res.substring(0, res.length()-1);
		res += ")";
		return res;
	}
	
	/**
	 * 检测矩阵是否对称
	 * @param mat
	 */
	public static void checkMatrixIsSymmetric(double[][] mat){
		boolean flag = true;
		int sum =0;
		for(int i=0;i<mat.length;i++){
			for(int j=0;j<mat[i].length;j++){
				if(mat[i][j] != mat[j][i]){
					flag = false;
					break;
				}
				if(mat[i][j] != 0){
					sum++;
				}
			}
		}
		System.out.println("矩阵对称吗？ "+(flag?"对称":"不对称"));
		System.out.println("边数为："+sum/2);
	}
	
	public static String getMinAndMax(double[] array){
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		double sum = 0.0;
		for(int i=0;i<array.length;i++){
			if(array[i] < min){
				min = array[i];
			}
			if(array[i] > max){
				max = array[i];
			}
			sum += array[i];
		}
		return "（"+min+" ~ "+max+"),avg: "+(sum/array.length);
	}
}
