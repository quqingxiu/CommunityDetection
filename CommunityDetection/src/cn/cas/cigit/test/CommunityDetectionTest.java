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
 * ��������㷨������
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
		int trueDetectSize = 0;		//������ȷ�Ķ�����
		//ͳ�ƽ����Ϣ
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
			System.out.println("���ݴ���");
			return;
		}
		
		System.out.println("��ȷ���ֵĶ�����Ϊ��"+trueDetectSize+" , "+CollectionUtil.toString(results));
		
		//�����׼����Ϣ
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
			System.out.println("����ı�׼����ϢֵΪ��"+0);
		}else{
			nmi = topPart/Math.sqrt(buttomLeftPart*buttomRightPart);
			System.out.println("����ı�׼����ϢֵΪ��"+nmi);
		}
	}
	
	@Test
	public void testPolblogsDataset() throws Exception{
		long start = System.currentTimeMillis();
		ss = new DataSource(new GMLDatasetParse("data/polblogs/polblogs.gml"));
		File file = new File(FileUtils.getFileAbsolutePath("/data/result/polblogs.txt"));
		FileUtils.writeFileByBytes("#ruleName coeff rate totalTimes res",file, false);
		
		//��ʼ����beta���¹���
		double[] betaArr = {0.3,0.5,0.75,0.9,0.99};
		iteraTest("polblogs",betaArr,"beta",0.1,0.01,0.015,3,1000,1e-5);
		
		//��ʼ����alpha���¹���
		double[] alphaArr = {(double)1/2,(double)1/3,(double)1/4,(double)1/1.5,(double)1/1.1,0.99};
		iteraTest("polblogs",alphaArr,"alpha",0.1,0.01,0.015,3,1000,1e-5);
		long end = System.currentTimeMillis();
		System.out.println("����������ܺ�ʱ��"+(end-start)/1000+"��");
		
	}
	
	public void iteraTest(String datasetName,double[] coeffArr,String ruleName,double maxRate,double startRate,double deltaRate,int selectedSize,int maxIter,double maxError) throws Exception{
		int totalTimes = 50;		//�㷨ִ���ܴ���
		for(double coeff:coeffArr){		//ѡȡ��ͬ��betaϵ��
			if("beta".equals(ruleName)){
				nmf = new NMFactorization(new BetaUpdateRule(coeff));
			}else if("alpha".equals(ruleName)){
				nmf = new NMFactorization(new AlphaUpdateRule(coeff));
			}
			for(double rate=startRate;rate<=maxRate;rate+=deltaRate){		//���벻ͬ���ʵı�����Ϣ
				System.out.println("\n��ʼ"+ruleName+"�����㷨��ϵ����"+coeff+",rate:"+String .format("%.3f",rate));
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
				System.out.println("ʹ��"+ruleName+"���¹���,"+ruleName+"ϵ��Ϊ��"+coeff+",������"+String .format("%.3f",rate)+"%������Ϣʱ,ִ��"+totalTimes+"�κ��NMIֵΪ��"+res);
			}
		}
	}
	
	/**
	 * �������
	 * @param usedPercent ʹ����Ϊ��ʶ���ӵİٷֱ�
	 * @param selectedSize ÿ�ε����޸ĵ�������
	 * @param maxIter ����������
	 * @param maxError ������
	 * @throws Exception
	 */
	public double communityDetection(double usedPercent,int selectedSize,int maxIter,double maxError) throws Exception{
		Network nw = ss.refreshNetwork();
		Matrix adjacencyMat = nw.getAdjacencyMat();
		
		int nodeSize = nw.getNodeSize();
		int iterNoChangeNum = 0;	//û�н��ı�����������������������
		int totalLinkSize = (int)(usedPercent*0.5*0.01*nodeSize*(nodeSize-1));
		int times = 0;
		Matrix X = Matrix.random(adjacencyMat.getRowDimension(), nw.getCommunitySize());
		X.arrayTimesEquals(new Matrix(adjacencyMat.getRowDimension(),nw.getCommunitySize(),0.01));
		
		System.out.println("ִ����������㷨,����������"+totalLinkSize);
		double nmi = 0;
		while(times < 100){
			System.out.println("��"+(times+1)+"�ε�����");
			X = nmf.executeNMF(adjacencyMat,X, maxIter,maxError);
			boolean modifyFlag = nw.getConmunityOfNode(X.getArray());
			nmi = nw.calculateNMI();		//����ִ�н���ı�׼����Ϣֵ
			if(modifyFlag && !NMFactorization.isFinished()){
				iterNoChangeNum = 0;
			}else{
				iterNoChangeNum ++;
			}
			if(iterNoChangeNum >= 5){	//����5�ε���û�н��ı�����������������ֽ�
				break;
			}
			double[] entropys = nw.calcEntropyOfNode(X.getArray());
			nw.modifyNetworkByConnectionStrategy(entropys,totalLinkSize,selectedSize);
			times++;
		}
		return nmi;
	}
}
