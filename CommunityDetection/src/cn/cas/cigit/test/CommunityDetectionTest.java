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
 * ��������㷨������
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
		System.out.println("����������ܺ�ʱ��"+(end-start)/1000);
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
		DataSource ss = new DataSource(new AmazonDatasetParse());
		Network nw = ss.getNetWork();
		Matrix adjacencyMat = nw.getAdjacencyMat();
		
		int nodeSize = nw.getNodeSize();
		int iterNoChangeNum = 0;	//û�н��ı�����������������������
		int totalLinkSize = (int)(usedPercent*0.5*0.01*nodeSize*(nodeSize-1));
		int times = 0;
		Matrix X = Matrix.random(adjacencyMat.getRowDimension(), nw.getCommunitySize());
		X.arrayTimesEquals(new Matrix(adjacencyMat.getRowDimension(),nw.getCommunitySize(),0.01));
		
		System.out.println("\n��ʼִ����������㷨,����������"+totalLinkSize);
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
