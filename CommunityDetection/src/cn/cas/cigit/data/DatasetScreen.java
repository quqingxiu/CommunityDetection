package cn.cas.cigit.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.cas.cigit.parse.DatasetParse;

/**
 * 数据筛选类
 * @author qqx
 *
 */
public class DatasetScreen {
	public DatasetScreen(){
	}
	
	public void screenData(){
		File file = new File(DatasetParse.getFileAbsolutePath("data/amazon/com-amazon.top5000.cmty.txt"));
		Set<String> nodeSet = new HashSet<String>();
		List<String> list = FileUtils.readFileByLine(file);
		List<String> newCmty = new ArrayList<String>();
		int sum = 0;
		int size = 0;
		for(String line : list){
			if(line.startsWith("#")  || newCmty.contains(line)){
				continue;
			}
			String[] ss = line.split("\t");
			if(ss.length >= 50){
				for(String s : ss){
					nodeSet.add(s);
				}
				newCmty.add(line);
				sum+= ss.length;
				size++;
			}
		}
		FileUtils.writeFileByBytes(newCmty, new File(DatasetParse.getFileAbsolutePath("data/amazon/formal-amazon.top705.cmty.txt")));
		System.out.println("选择的社区数为："+size+" ,点总数为："+sum+" ,不重复点数："+nodeSet.size());
		
		file = new File(DatasetParse.getFileAbsolutePath("data/amazon/com-amazon.ungraph.txt"));
		list = FileUtils.readFileByLine(file);
		newCmty.clear();
		for(String line : list){
			if(line.startsWith("#")){
				continue;
			}
			String[] ss = line.split("\t");
			if(nodeSet.contains(ss[0]) && nodeSet.contains(ss[1])){
				newCmty.add(line);
			}
		}
		FileUtils.writeFileByBytes(newCmty, new File(DatasetParse.getFileAbsolutePath("data/amazon/formal-amazon.ungraph.txt")));
		System.out.println("网络边数大小为："+newCmty.size());
	}
	
	public static void main(String[] args) {
		DatasetScreen sc = new DatasetScreen();
		sc.screenData();
		
	}
	

}
