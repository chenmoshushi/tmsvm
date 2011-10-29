import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * SVM Ԥ�����
 * ֧�ֶ�ģ�͵�Ԥ�⡣
 * �����ļ�û���ض���ʽ��Ԥ��ʱ��ָ�����ֶεķָ������ִʵķָ���š�ÿһ��ģ��Ԥ����ֶ�
 * @author zhangzhilin.pt zhzhl202@163.com
 * @date 2011-10-26
 */
public class SvmPredict {
	
	public static void main(String []args){
		
		/**
		 * �������Ӽ��ʾ������������ʾ���Ǵ��ļ��ж����������ݣ�Ȼ��ѽ��д�뵽�ļ��С�
		 * ��������ݸ�ʽΪ thread_id,group_id,subject,content��������id,��id,����,����
		 */
		SvmPredict libsvm = new SvmPredict();
		String in_filename = "D:/��֪��/Դ����/libsvm_java/model/im_info/example.test"; //�����ļ�
		String tc_splitTag="\t"; //��������ݸ�������֮��ķָ����
		String str_splitTag ="\\^"; //��������ݾ����ִʺ󣬸����ʵķָ����
		String str_connentTag ="^"; //�����Ҫ�Զ�������ۺ���һ�����֣���Ҫ������������ʵ�����str_splitTag����һ�£�������Ϊ��java��^��\\^����ͬ������Ҫ�ֿ�д
		String out_filename = "D:/��֪��/Դ����/libsvm_java/model/im_info/result.txt"; //���������ļ�			
	
		try {			
			BufferedReader input = new BufferedReader(new FileReader(in_filename));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out_filename)));
			while(true){
				String line = input.readLine();	
				if(line == null) 
					break;
				double[] post_sc = libsvm.cal_post_sc(line,tc_splitTag, str_splitTag, str_connentTag); //�������ӵĵ÷֣�����֣�����+���ݷ֣�
				for(double score :post_sc)
					output.writeBytes(score+"\t");
				output.writeBytes("\n");
			}
			input.close();
			output.close();	
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	
	
	private String model_main_path = "D:/��֪��/Դ����/libsvm_java/model/im_info/"; //ģ�ʹ�ŵ���·��

	private String[] dic_path ={model_main_path+"im.key"}; //�ʵ��б�
	private String[] model_path ={model_main_path+"im.model"}; //ģ���б�
	private int[][] indexes={ {6} };//����ģ�Ͷ�Ӧ���ı����Ǳ��⣬���������ı����±�Ϊ2������+����ģ�Ͷ�Ӧ���ı����Ǳ�������ݣ����������ı����±�Ϊ2��3
	
	private List< Map<String,Integer> >  dics; //����ʵ�,����+���ݴʵ�
	private List< svm_model> models; 
	
	public SvmPredict(){
		init_model();
	}
	/**
	 * 	��ʼ�����֣�����ģ�ͣ������ʵ��SVMģ�͡�
	 */
	public void init_model(){
		//����ʵ�
		int k = dic_path.length;
		dics = new ArrayList< Map<String,Integer> >();
		models = new ArrayList< svm_model >();

		try {
			for(int i =0;i<k;i++){
				dics.add(read_dic(dic_path[i]));
				models.add(svm.svm_load_model(model_path[i]));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
/**
 * �����������ڳ��򣬸������룬�Լ����ַָ���š�
 * Ȼ���������е�ģ�ͽ���Ԥ�����
 * @param line ������ı�
 * @param tc_splitTag �����ı���Ϊ�������֣���������֮��ķָ����
 * @param str_splitTag �ִʺ��ı��ķָ���š�
 * @param str_connentTag ���Ҫ������ַ���һ��Ԥ�⣬��Ҫ�������ӷ���������һ��ʵ����str_splitTag��str_connentTag��һ��������������java�����߲�����ͬ ^ \\^
 * @return
 */
	public double[] cal_post_sc(String line,String tc_splitTag,String str_splitTag,String str_connentTag){
		String []input_text = line.split(tc_splitTag);
		int k = dics.size();
		double[] scores = new double[k];
		for(int i=0;i<k;i++){
			int[] index = indexes[i];
			Map<String,Integer> dic = dics.get(i);
			svm_model model = models.get(i);
			String text = merge_text(input_text, index,str_connentTag);
			scores[i] = cal_svm_sc(text.split(str_splitTag), dic, model);

		}
		return scores;

	}
	
	/**
	 * ����SVMģ�͵ķ���.֧�ֶ�ģ��
	 * @param text_arr  ������ı�������ΪString[]��ʽ
	 * @param dic �ֵ�
	 * @param model ģ��
	 * @return
	 */
	public double cal_svm_sc(String[] text_arr,Map<String,Integer> dic,svm_model model){
		Vector text = new Vector<String>(); //�ı�Vector
		for(int i =0;i<text_arr.length;i++){
			if (text_arr[i].trim().length()>0)
				text.add(text_arr[i]);
		}

		int nr_class = model.nr_class;
		double[] des_values =new double[nr_class*(nr_class-1)/2];
		svm_node[] x = cons_pro_for_svm(text, dic);
		if (x==null) //������ı��Ĵ��ڴʵ� ��û�г��ֹ����򷵻�һ����С��ֵ��
			return -10;
		double pred_result = svm.svm_predict_values(model,x,des_values);
		if (nr_class>2)
			return  pred_result;
		else 
			return des_values[0];

	}

	/**
	 * ����SVM�ض�������ṹԤ�����
	 * @param x SVM�����ض��������ʽ
	 * @param dic �ʵ� 
	 * @param model ģ��
	 * @return
	 */
	public double predict_svm_sc(svm_node[] x,Map<String,Integer> dic,svm_model model){
		int nr_class = model.nr_class;
		double[] des_values =new double[nr_class*(nr_class-1)/2];

		if (x==null) //������ı��Ĵ��ڴʵ� ��û�г��ֹ����򷵻�һ����С��ֵ��
			return -10;
		double pred_result = svm.svm_predict_values(model,x,des_values);
		if (nr_class>2)
			return pred_result;
		else 
			return des_values[0];
	}
	
	/**
	 * �Ѽ����ֿ����ı������������������ӷ���Ҫ���Ժ�ָ�ķ�����ͬ
	 * @param original_text
	 * @param indexes
	 * @param str_splitTag
	 * @return
	 */
	public String merge_text(String[] original_text,int[] indexes,String str_connentTag){
		String text="";
		for(int i =0;i<indexes.length;i++)
			text+=str_connentTag+original_text[indexes[i]];
		return text;
	}
	
	/**
	 * ����ʵ�
	 * @param dic_path
	 * @return
	 */
	public Map<String,Integer> read_dic(String dic_path){
		Map<String,Integer>  dic = new HashMap<String,Integer>();//�ʵ�洢��Map
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(dic_path));
			int count=0;
			while(true){
				String line = input.readLine();
				if(line == null) break;
				count+=1;
				String[] temp = line.split("\t");
				if (temp.length>1)
				dic.put(temp[0], Integer.parseInt(temp[1]));
				else dic.put(temp[0], count);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dic;
	}
	
	/**
	 * ����������ı����Լ�����Ĵʵ䣬����SVMģ�͵��ض�����
	 * �ú�����Ŀ�ľ��ǹ����ı������������������й�һ���������Ǵ˴�Ϊ�����Ч�ʣ�����Map����Vector��ֻ�洢��0ֵ��
	 * @param text ����洢��Ϊһ�����Ĵ�
	 * @param dic ���ô洢��Ϊ�ʵ䣬String Ϊ�ʣ�IntegerΪ��Id
	 * @return ���ص���SVM�ض�������ṹ
	 */
	public svm_node[] cons_pro_for_svm(Vector<String> text,Map<String,Integer> dic){
		Map feature_map = new HashMap<Integer,Integer>();
		
		//�����ı���ÿ���ʶ�Ӧ��ʵ��λ�ã��Լ���Ӧ�Ĵ�Ƶ��
		for(int i =0;i<text.size();i++){
			String term = (text.elementAt(i).toString()).trim();
	        if (dic.containsKey(term)){ //��ѯdic���Ƿ�����ô�
	        	int index = dic.get(term); //�������������feature_map����Ӧλ�ü�1
	        	if (feature_map.containsKey(index)){ //������������һ���Ǹô��Ѿ��ڴʵ��У�
	        		int  count = (Integer) feature_map.get(index);
	        		feature_map .put(index, count+1);
	        	}
	        	else  //��һ���Ǹô�δ�ڴʵ���
	        		feature_map .put(index, 1);
	        }
		}
		//������ĵ�����������ģ
		double vec_sum = 0.0;
		Object[] keys = feature_map.keySet().toArray();
		for(int i=0;i<keys.length;i++){
			vec_sum += (Integer)feature_map.get(keys[i])*(Integer)feature_map.get(keys[i]);
		}
	    double vec_length=Math.sqrt(vec_sum);
	    
	    //��һ��������SVMģ�͵�����
	    svm_node[] x=null;
	    Arrays.sort(keys); //��feature_map�е�key����������Ҫ��Ϊ�˱�֤�����SVM��ʽ��Index���������С�
	    if(vec_length>0){
	    	int m = keys.length;
			 x= new svm_node[m]; //SVMģ�͵������ʽ
			/**�˴�Ϊ����SVM�����ʽ�ľ���**/
			//�����ı��еĴʳ��ֵĴ�Ƶ��
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = (Integer)keys[j];
				x[j].value = (double)((Integer) feature_map.get(keys[j])/vec_length); //�˴�Ҫ���й�һ��
			}
	    }
		
	    return x;
	}

}
