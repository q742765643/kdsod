package com.xugu.fetl.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xugu.fetl.util.FetlUtil;


public class AnalysisData implements Runnable{
	private String url;
	private int commitCount;
	private List<byte[]> data;
	private List<Integer> columnTypes;
	private String preSql;
	public static Logger log = Logger.getLogger(AnalysisData.class);
	public AnalysisData(String url,int commitCount,List<byte[]> data,String preSql,List<Integer> columnTypes){
		this.url = url;
		this.commitCount = commitCount;
		this.data = data;
		this.columnTypes = columnTypes;
		this.preSql = preSql;
	}
	
	@Override
	public void run(){    
        if(data != null && data.size() != 0){
        	Connection con = FetlUtil.get_conn(url+"&char_set=utf8");
        	int lineCount = 0;
	        int index = 0;
	        int length = 0;
	        List<byte[][]> datas = new ArrayList<>();
	        PreparedStatement ps = null;
	        byte[][] rowData = null;
	        try{
	        	int columnCount = columnTypes.size();
				int signCount = columnCount;
				signCount = (signCount+3)/4;
				byte[] signbytes = new byte[signCount];
				ps = con.prepareStatement(preSql);
				for(byte[] bytes:data){
					index += signCount;
					for(int i = 0;i < signCount;i++){
						signbytes[i] = bytes[i];
					}
					rowData = new byte[columnCount][];
					for(int i = 1 ;i <= columnCount; i++){
						byte[] columnData = null;
						int idx = (i-1)/4;
						int off = (i-1)%4;
						int sign = signbytes[idx] & signByte.getSign(off);
						switch ( sign >>> off*2){
							case 0:
								length = byte2Short(bytes[index], bytes[index+1]);
								index += 2;
								columnData = new byte[length];
								System.arraycopy(bytes, index, columnData, 0, length);
								index += length;
								ps.setObject(i, columnData, columnTypes.get(i-1));
								rowData[i-1] = columnData;
								break;
							case 1:
								ps.setString(i, null);
								rowData[i-1] = null;
								break;
							case 2:
								length = byte2Int(bytes[index], bytes[index+1], bytes[index+2], bytes[index+3]);
								index += 4;
								columnData = new byte[length];
								System.arraycopy(bytes, index, columnData, 0, length);
								index += length;
								ps.setObject(i, columnData, columnTypes.get(i-1));
								rowData[i-1] = columnData;
								break;
							case 3:
								columnData = new byte[0];
								ps.setObject(i, columnData, columnTypes.get(i-1));
								rowData[i-1] = columnData;
								break;
					   }
					}
					ps.addBatch();
					lineCount++;
					index = 0;
					datas.add(rowData);
					if(lineCount % commitCount == 0){
						try{
							ps.executeBatch();
							datas.clear();
						} catch (SQLException e) {
							if(commitCount > 0) {
								impData(ps,datas);
								datas.clear();
							}else {
								log.error(e.getMessage()+"\nsql  : "+sql(preSql, rowData));
							}
						}
					}
				}
				ps.executeBatch();
			} catch (SQLException e){
				if(commitCount > 0) {
					impData(ps,datas);
					datas.clear();
				}else {
					log.error(e.getMessage()+"\nsql  : "+sql(preSql, rowData));
				}
			} catch (Exception e){
				log.error(e.getMessage());
			} finally {
				data.clear();
				FetlUtil.closePs(ps);
				FetlUtil.closeConn(con);
			}
        }
	}	
	public void impData(PreparedStatement ps, List<byte[][]> datas){
		byte[][] rowData = new byte[datas.get(0).length][];
		try {
			for(int k = 0; k < datas.size(); k++){
			    for(int i = 0; i < datas.get(k).length;i++){
				    if(datas.get(k)[i] == null){
				    	ps.setString(i+1,null);
				    }else{
				    	ps.setObject(i+1, datas.get(k)[i], this.columnTypes.get(i));
				    }
				    rowData[i] = datas.get(k)[i];
			    }
			    ps.execute();
			}
		} catch (SQLException e) {
			log.error(e.getMessage()+"\nsql  : "+sql(preSql, rowData));
		}
	}
//	public void impData(PreparedStatement ps, List<byte[][]> datas){
//		List<byte[][]> data100 = new ArrayList<>();
//		try{
//		    for(int k = 0; k < datas.size(); k++){
//			    for(int i = 0; i < datas.get(k).length;i++){
//				    if(datas.get(k)[i] == null){
//				    	ps.setString(i+1,null);
//				    }else{
//				    	ps.setObject(i+1, datas.get(k)[i], this.columnTypes.get(i));
//				    }
//			    }
//			    ps.addBatch();
//			    data100.add(datas.get(k));
//			    if((k+1)%100 == 0){
//			    	try{
//			    		ps.executeBatch();
//			    		data100.clear();
//			    	} catch (SQLException e){
//			    		if(e.getErrorCode() == 16005 || e.getErrorCode() == 13001){
//			    			for(byte[][] data:data100){
//			    				impData(ps,data,false);
//			    			}
//							data100.clear();
//						} else {
//							log.error(e.getMessage()+" sql  : "+preSql);	
//						}
//			    	}
//			    }
//		    }
//		    ps.executeBatch();
//		} catch (SQLException e){
//			if(e.getErrorCode() == 16005 || e.getErrorCode() == 13001){
//				for(byte[][] data:data100){
//    				impData(ps,data,false);
//    			}
//			} else {
//				log.error(e.getMessage()+" sql  : "+preSql);	
//			}
//		}
//	}
	
//	public void impData(PreparedStatement ps, byte[][] data, boolean flag){
//		try{
//			for(int i = 0; i < data.length;i++){
//			    if(data[i] == null){
//			    	if(flag){
//			    		ps.setString(i+1, "");
//			    	}else{
//			    		ps.setString(i+1,null);
//			    	}
//			    }else{
//			    	ps.setObject(i+1, data[i], this.columnTypes.get(i));
//			    }
//			}
//			ps.execute();
//		} catch (SQLException e){
//			if(e.getErrorCode() == 16005){
//				impData(ps, data, true);
//			} else{
//				StringBuffer sb = new StringBuffer();
//				for(byte[] bb: data) {
//					if(bb != null) {
//						try {
//							sb.append(new String(bb,"UTF-8")).append(" ");
//						} catch (UnsupportedEncodingException e1) {
//							e1.printStackTrace();
//						} 
//					}else {
//						sb.append("null").append(" ");
//					}
//				}
//				log.error(e.getMessage()+"\nsql  : "+preSql +"\n"+" param : "+ sb.toString());
//			}
//		} 
//	}
	
	
	public enum signByte{
		b0sign(0x03),b1sign(0x0c),b2sign(0x30),b3sign(0xc0);
		private int val;
		private signByte(int val){
			this.val = val;
		}
		public static int getSign(int i){
			int ret = 0x00;
			switch (i){
				case 0:
					ret =  b0sign.val;
					break;
				case 1:
					ret =  b1sign.val;
					break;
				case 2: 
					ret =  b2sign.val;
					break;
				case 3:
					ret =  b3sign.val;
					break;
			}
			return ret;
		}
	}
	public int byte2Int(byte b0,byte b1,byte b2,byte b3){
		return (int)((b0&0xff)<<24|(b1&0xff)<<16|(b2&0xff)<<8|(b3&0xff));
	}
	public short byte2Short(byte b0,byte b1){
		return (short)((b0&0xff)<<8|(b1&0xff));
	}
	public String sql(String sql,byte[][] rowData) {
		try {
			for(int i = 0;i < rowData.length;i++) {
				String fieldValue = "null";
				switch (columnTypes.get(i)) {
					case 4:
					case -6:
					case 5:
					case 2:
					case 6:
					case 8:
						if(rowData[i] != null) {
							fieldValue = new String(rowData[i],"utf-8");
						}
						break;
					case 2004:
						fieldValue = "<blob>";
						break;
					case 2005:
						fieldValue = "<clob>";
						break;
					case -2:
						fieldValue = "<binary>";
						break;
					default:
						if(rowData[i] != null) {
							fieldValue = "'"+new String(rowData[i],"utf-8")+"'";
						}
						break;
				}
				sql = sql.replaceFirst("\\?", fieldValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		    log.error(e.getMessage());
		}
		return sql;
	}
}
