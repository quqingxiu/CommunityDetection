package cn.cas.cigit.test;

import org.junit.Before;
import org.junit.Test;

import Jama.Matrix;
import cn.cas.cigit.data.DataSource;
import cn.cas.cigit.model.Network;
import cn.cas.cigit.nmf.BetaUpdateRule;
import cn.cas.cigit.nmf.NMFactorization;
import cn.cas.cigit.parse.AmazonDatasetParse;

/**
 * 社区检测算法测试类
 * @author qqx
 *
 */
public class CommunityDetectionTest {
	private NMFactorization nmf;
	
	@Before
	public void inital(){
		double beta = 0.5;
		nmf = new NMFactorization(new BetaUpdateRule(beta));
	}
	
	@Test
	public void testAmazonDataset() throws Exception{
		long start = System.currentTimeMillis();
		double nmi = communityDetection(0.001, 10,100,1e-5);
		long end = System.currentTimeMillis();
		System.out.println("计算结束，总耗时："+(end-start)/1000);
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
		DataSource ss = new DataSource(new AmazonDatasetParse());
		Network nw = ss.getNetWork();
		Matrix adjacencyMat = nw.getAdjacencyMat();
		
		int nodeSize = nw.getNodeSize();
		int iterNoChangeNum = 0;	//没有结点改变所属社区的连续迭代次数
		int totalLinkSize = (int)(usedPercent*0.5*0.01*nodeSize*(nodeSize-1));
		int times = 0;
		Matrix X = Matrix.random(adjacencyMat.getRowDimension(), nw.getCommunitySize());
		X.arrayTimesEquals(new Matrix(adjacencyMat.getRowDimension(),nw.getCommunitySize(),0.01));
		
		System.out.println("\n开始执行社区检测算法,链接总数："+totalLinkSize);
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
