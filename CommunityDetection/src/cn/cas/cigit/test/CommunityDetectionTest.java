package cn.cas.cigit.test;

import java.io.File;

import org.junit.Test;

import Jama.Matrix;
import cn.cas.cigit.data.CollectionUtil;
import cn.cas.cigit.data.DataSource;
import cn.cas.cigit.data.FileUtils;
import cn.cas.cigit.model.Network;
import cn.cas.cigit.model.Node;
import cn.cas.cigit.nmf.AlphaUpdateRule;
import cn.cas.cigit.nmf.BetaUpdateRule;
import cn.cas.cigit.nmf.NMFactorization;
import cn.cas.cigit.parse.GMLDatasetParse;

/**
 * 社区检测算法测试类
 * @author qqx
 *
 */
public class CommunityDetectionTest {
	private NMFactorization nmf;
	private DataSource ss ;
	
	public static void main(String[] args) {
		double[][] arr = {{2,1},{2,2}};
		Matrix mat = new Matrix(arr);
		mat.print(1, 0);
		double[][] arr2 = mat.getArrayCopy();
		arr2[0][1] = 90;
		mat.print(1, 0);
	}
	
	@Test
	public void testPolblogs_single() throws Exception{
		ss = new DataSource(new GMLDatasetParse("data/polblogs/polblogs.gml"));
		nmf = new NMFactorization(new AlphaUpdateRule(0.99));
		double imn = communityDetection(0.01,3,1000,1e-5);
	}
	
	@Test
	public void testNormalMutualInf(){
		int K = 2,nodeSize = 1490;
		int[][] nij = {{613,77},{100,700}};
		int[] groundTruth = {732,758};
		int[] results = new int[K];
		int trueDetectSize = 0;		//划分正确的顶点数
		//统计结果信息
		int total = 0;
		for(int i=0;i<K;i++){
			trueDetectSize += nij[i][i];
			int sum = 0;
			for(int j=0;j<K;j++){
				sum += nij[j][i];
			}
			results[i] = sum;
			total += sum;
		}
		if(total != nodeSize){
			System.out.println("数据错误！");
			return;
		}
		
		System.out.println("正确划分的顶点数为："+trueDetectSize+" , "+CollectionUtil.toString(results));
		
		//计算标准互信息
		double nmi = 0;
		double topPart = 0.0;
		double buttomLeftPart = 0.0;
		double buttomRightPart = 0.0;
		for(int i=0;i<K;i++){
			for(int j=0;j<K;j++){
				if(groundTruth[i]*results[j]*nij[i][j] == 0){
					continue;
				}
				double temp = (double)(nodeSize*nij[i][j])/(groundTruth[i]*results[j]);
				topPart +=nij[i][j]*Math.log(temp);
			}
			if(groundTruth[i]!=0){
				buttomLeftPart += groundTruth[i]*Math.log((double)groundTruth[i]/nodeSize);
			}
			if(results[i]!=0){
				buttomRightPart += results[i]*Math.log((double)results[i]/nodeSize);
			}
		}
		if(buttomLeftPart*buttomRightPart == 0){
			System.out.println("结果的标准互信息值为："+0);
		}else{
			nmi = topPart/Math.sqrt(buttomLeftPart*buttomRightPart);
			System.out.println("结果的标准互信息值为："+nmi);
		}
	}
	
	@Test
	public void testPolblogsDataset() throws Exception{
		long start = System.currentTimeMillis();
		ss = new DataSource(new GMLDatasetParse("data/polblogs/polblogs.gml"));
		File file = new File(FileUtils.getFileAbsolutePath("/data/result/polblogs.txt"));
		FileUtils.writeFileByBytes("#ruleName coeff rate totalTimes res",file, false);
		
		//开始测试beta更新规则
		double[] betaArr = {0.3,0.5,0.75,0.9,0.99};
		iteraTest("polblogs",betaArr,"beta",0.1,0.01,0.015,3,1000,1e-5);
		
		//开始测试alpha更新规则
		double[] alphaArr = {(double)1/2,(double)1/3,(double)1/4,(double)1/1.5,(double)1/1.1,0.99};
		iteraTest("polblogs",alphaArr,"alpha",0.1,0.01,0.015,3,1000,1e-5);
		long end = System.currentTimeMillis();
		System.out.println("计算结束，总耗时："+(end-start)/1000+"秒");
		
	}
	
	public void iteraTest(String datasetName,double[] coeffArr,String ruleName,double maxRate,double startRate,double deltaRate,int selectedSize,int maxIter,double maxError) throws Exception{
		int totalTimes = 50;		//算法执行总次数
		for(double coeff:coeffArr){		//选取不同的beta系数
			if("beta".equals(ruleName)){
				nmf = new NMFactorization(new BetaUpdateRule(coeff));
			}else if("alpha".equals(ruleName)){
				nmf = new NMFactorization(new AlphaUpdateRule(coeff));
			}
			for(double rate=startRate;rate<=maxRate;rate+=deltaRate){		//加入不同比率的背景信息
				System.out.println("\n开始"+ruleName+"更新算法，系数："+coeff+",rate:"+String .format("%.3f",rate));
				double[] nmis = new double[totalTimes];
				int time = 0;
				while(time < totalTimes){
					double nmi = communityDetection(rate,selectedSize,maxIter,maxError);
					nmis[time] = nmi;
					time++;
				}
				String res = CollectionUtil.getMinAndMax(nmis);
				File file = new File(FileUtils.getFileAbsolutePath("/data/result/"+datasetName+".txt"));
				FileUtils.writeFileByBytes("\n"+ruleName+" "+coeff+" "+ String .format("%.3f",rate)+" "+totalTimes+" "+res,file, true);
				System.out.println("使用"+ruleName+"更新规则,"+ruleName+"系数为："+coeff+",当加入"+String .format("%.3f",rate)+"%背景信息时,执行"+totalTimes+"次后的NMI值为："+res);
			}
		}
	}
	
	/**
	 * 社区检测
	 * @param usedPercent 使用人为标识连接的百分比
	 * @param selectedSize 每次迭代修改的连接数
	 * @param maxIter 最大迭代次数
	 * @param maxError 最大误差
	 * @throws Exception
	 */
	public double communityDetection(double usedPercent,int selectedSize,int maxIter,double maxError) throws Exception{
		Network nw = ss.refreshNetwork();
		Matrix adjacencyMat = nw.getAdjacencyMat();
		
		int nodeSize = nw.getNodeSize();
		int iterNoChangeNum = 0;	//没有结点改变所属社区的连续迭代次数
		int totalLinkSize = (int)(usedPercent*0.5*0.01*nodeSize*(nodeSize-1));
		int times = 0;
		Matrix X = Matrix.random(adjacencyMat.getRowDimension(), nw.getCommunitySize());
		X.arrayTimesEquals(new Matrix(adjacencyMat.getRowDimension(),nw.getCommunitySize(),0.01));
		
		System.out.println("执行社区检测算法,链接总数："+totalLinkSize);
		double nmi = 0;
		while(times < 100){
			System.out.println("第"+(times+1)+"次迭代：");
			X = nmf.executeNMF(adjacencyMat,X, maxIter,maxError);
			boolean modifyFlag = nw.getConmunityOfNode(X.getArray());
			nmi = nw.calculateNMI();		//计算执行结果的标准互信息值
			if(modifyFlag && !NMFactorization.isFinished()){
				iterNoChangeNum = 0;
			}else{
				iterNoChangeNum ++;
			}
			if(iterNoChangeNum >= 5){	//连续5次迭代没有结点改变所属社区，则结束分解
				break;
			}
			double[] entropys = nw.calcEntropyOfNode(X.getArray());
			nw.modifyNetworkByConnectionStrategy(entropys,totalLinkSize,selectedSize);
			times++;
		}
		return nmi;
	}
}
