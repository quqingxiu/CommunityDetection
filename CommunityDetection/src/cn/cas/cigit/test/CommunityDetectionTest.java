package cn.cas.cigit.test;

import java.io.File;

import org.junit.Test;

import Jama.Matrix;
import cn.cas.cigit.data.CollectionUtil;
import cn.cas.cigit.data.DataSource;
import cn.cas.cigit.data.FileUtils;
import cn.cas.cigit.model.Network;
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
	public void testPolblogsDataset() throws Exception{
		long start = System.currentTimeMillis();
		ss = new DataSource(new GMLDatasetParse("data/polblogs/polblogs.gml"));
		File file = new File(FileUtils.getFileAbsolutePath("/data/result/polblogs.txt"));
		FileUtils.writeFileByBytes("#ruleName coeff rate totalTimes res",file, false);
		
		//��ʼ����beta���¹���
		double[] betaArr = {0.25,0.5,0.75,1.0};
		iteraTest("polblogs",betaArr,"beta",0.01,0.001,0.001,3,1000,1e-5);
		
		//��ʼ����alpha���¹���
		double[] alphaArr = {2,3,4,5,6,7};
		iteraTest("polblogs",alphaArr,"alpha",0.01,0.001,0.001,3,1000,1e-5);
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
			for(double rate=startRate;rate<maxRate;rate+=deltaRate){		//���벻ͬ���ʵı�����Ϣ
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
