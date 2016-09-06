package cn.cas.cigit.test;

import org.junit.Test;

import Jama.Matrix;
import cn.cas.cigit.data.ArrayUtil;
import cn.cas.cigit.data.DataSource;
import cn.cas.cigit.model.Network;
import cn.cas.cigit.nmf.NMFactorization;
import cn.cas.cigit.parse.AmazonDatasetParse;
import cn.cas.cigit.parse.GMLDatasetParse;

public class NetworkTest {
	public static void main(String[] args) throws Exception {
		double[] arr = new double[]{1,4,3,8};
		Name n = new Name(arr);
		n.change(9);
		n.show();
		for(int i=0;i<arr.length;i++){
			System.out.print(arr[i]+" ");
		}

	}
	
	@Test
	public void testCommunityDetection() throws Exception{
		DataSource ss = new DataSource(new GMLDatasetParse("data/football/football.gml"));
//		DataSource ss = new DataSource(new GMLDatasetParse("data/dolphin/dolphins.gml"));
		int time = 0;
		double[] nmis = new double[20];
		double rate = 1;
		while(time < 20){
			Network nw = ss.refreshNetwork();
			double nmi = nw.communityDetection(rate,3);
			nmis[time] = nmi;
			time++;
		}
		System.out.println("当加入"+rate+"%背景信息时，选取不同初始值，执行10次后的NMI值为："+ArrayUtil.getMinAndMax(nmis));
	}
	
	@Test
	public void testPolblogs() throws Exception{
		DataSource ss = new DataSource(new GMLDatasetParse("data/polblogs/polblogs.gml"));
		int time = 0;
		double[] nmis = new double[10];
		double rate = 0.01;
		while(time < 10){
			Network nw = ss.refreshNetwork();
			double nmi = nw.communityDetection(rate,3);
			nmis[time] = nmi;
			time++;
		}
		System.out.println("当加入"+rate+"%背景信息时，选取不同初始值，执行10次后的NMI值为："+ArrayUtil.getMinAndMax(nmis));
	}
	
	@Test
	public void testAmazonDataset() throws Exception{
		long start = System.currentTimeMillis();
		DataSource ss = new DataSource(new AmazonDatasetParse());
		Network nw = ss.getNetWork();
		double[][] arr = nw.getAdjacencyMat().getArray();
//		ArrayUtil.checkMatrixIsSymmetric(arr);
//		double nmi = nw.communityDetection(0.001, 10);
		long end = System.currentTimeMillis();
		System.out.println("计算结束，总耗时："+(end-start)/1000);
	}

	
	@Test
	public void testNMFactorization() throws Exception{
		Matrix sourceMat = DataSource.getMatrixFromFile("E:/JavaProject/CommunityDetection/data/football/d.txt");
		int sum =0 ;
		for(int i=0;i<sourceMat.getRowDimension();i++){
			for(int j=0;j<sourceMat.getColumnDimension();j++){
				if(sourceMat.get(i, j) == 1){
					sum +=2;
				}
			}
		}
		System.out.println("比率："+((double)sum/(sourceMat.getRowDimension()*sourceMat.getColumnDimension())));
	}

}
