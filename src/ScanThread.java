import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class ScanThread extends Thread {
	
	private static final String IMMV1STR = "<!-- WSMAN library";
	private static final String IMMV2STR = "var djConfig = {isDebug:false, parseOnLoad:true, locale:lang_conf , modulePaths : {'imm' : '../../imm'}}";
	private boolean useHttps;
	private String startIp = "";
	private String endIp = "";
	private int timeOut;
	
	public ScanThread(boolean useHttps, String startIp, String endIp, int timeOut) {
		this.useHttps = useHttps;
		this.startIp = startIp;
		this.endIp = endIp;
		this.timeOut = timeOut;
	}
	
	@Override
	public void run() {
		int start = getIpNumber(startIp);
		int end = getIpNumber(endIp);
		for(int i = start; i <= end; ++i) {
			String ip = getIpString(i);
			if(ip.endsWith(".255") || ip.endsWith(".0"))
				continue;
			boolean isImm = httpScan(ip);
			if(useHttps && !isImm)
				httpsScan(ip);
		}
	}
	
	public static int getIpNumber(String ip) {
		int result = 0;
		String[] strs = ip.split("\\.");
		for (int i = 0; i < 4; ++i) {
			result *= 256;
			result += Integer.valueOf(strs[i]);
		}
		return result;
	}
	
	public static String getIpString(int ip) {
		String[] values = new String[4];
		for (int i = 0; i < 4; ++i) {
			values[i] = String.valueOf(ip % 256);
			ip /= 256;
		}
		String result = values[3] + "." + values[2] + "." + values[1] + "." + values[0];
		return result;
	}
	
	private boolean httpScan(String ip) {
		BufferedReader in = null;
		try {
			URL url = new URL("http://" + ip);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6");
			uc.setConnectTimeout(timeOut);
			uc.setReadTimeout(timeOut);
			uc.connect();
			in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				if(matchHttp(line, ip)) {
					in.close();
					return true;
				}
			}
		} catch (Exception e) {
			//	do not output any error on screen.
		}
		if(in != null) {
			try {
				in.close();
			} catch (IOException e) {
				//	do not output any error on screen.
			}
		}
		return false;
	}
	
	private void httpsScan(String ip) {
		BufferedReader in = null;
		try {
			URL url = new URL("https://" + ip);
			SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new HttpsTrustManager() }, new SecureRandom());
            HttpsURLConnection uc = (HttpsURLConnection) url.openConnection();
            uc.setSSLSocketFactory(sc.getSocketFactory());
            uc.setHostnameVerifier(new HttpsHostnameVerifier());
			uc.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6");
			uc.setConnectTimeout(timeOut);
			uc.setReadTimeout(timeOut);
			uc.connect();
			in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				if(matchHttps(line, ip)) {
					in.close();
					return;
				}
			}
		} catch (Exception e) {
			//	do not output any error on screen.
		}
		if(in != null) {
			try {
				in.close();
			} catch (IOException e) {
				//	do not output any error on screen.
			}
		}
		return;
	}
	
	private boolean matchHttp(String page, String ip) {
		if(page.indexOf(IMMV1STR) != -1) {
			System.out.println("http://" + ip + "\t\t" + "IMMv1");
			return true;
		}
		else if(page.indexOf(IMMV2STR) != -1) {
			System.out.println("http://" + ip + "\t\t" + "IMMv2");
			return true;
		}
		return false;
	}
	
	private boolean matchHttps(String page, String ip) {
		if(page.indexOf(IMMV1STR) != -1) {
			System.out.println("https://" + ip + "\t\t" + "IMMv1");
			return true;
		}
		else if(page.indexOf(IMMV2STR) != -1) {
			System.out.println("https://" + ip + "\t\t" + "IMMv2");
			return true;
		}
		return false;
	}
	
}
