package com.akash.api;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}

@RestController
class Controller {

	public HashMap<String,String> cache = new HashMap<>();

	@RequestMapping("/")
	public String index() {
		return "Welcome to Exchange Rate Average calculator.\n Send your requests on /calculate";
	}

	@GetMapping("/calculate")
	public String calculate() {
		return "Use this api for sending POST Requests in JSON format: { \n\t\"range\":[\"YYYY-MM-DD\"(Start),\"YYYY-MM-DD\"(End)],\n\t \"currencyList\":[\"INR\",\"USD\"]\n}";
	}

	@PostMapping("/calculate")
	public String calculate(@RequestBody Map<String, String[]> body) {
		HashSet<String> validCurrencies = new HashSet<>(Arrays.asList("CAD", "HKD", "LVL", "PHP", "DKK", "HUF", "CZK", "AUD", "RON", "SEK", "IDR", "INR", "BRL", "RUB", "LTL", "JPY", "THB", "CHF", "SGD", "PLN", "BGN", "TRY", "CNY", "NOK", "NZD", "ZAR", "USD", "MXN", "EEK", "GBP", "KRW", "MYR", "HRK"));
		String clist[] = body.get("currencyList");
		TreeMap<String,Double> tm=new TreeMap<>();
		Gson g = new Gson();

		for(int i=0;i<clist.length;i++){
			if(validCurrencies.contains(clist[i]))
				tm.put(clist[i],0D);
			else
				return "Invalid currency : "+clist[i]+"\n";
		}

		LocalDate start,end;
		try {
			start = LocalDate.parse(body.get("range")[0]);
			end = LocalDate.parse(body.get("range")[1]);
		}catch(Exception e){
			return g.toJson(e.getMessage());
		}

		if(start.isAfter(end)){
			return "{ \n\t\"Error\" : \"Start date is greater than End Date.\"\n}";
		}

		if(start.isBefore(LocalDate.parse("2000-01-01"))){
			return "{ \n\t\"Error\" : \"Start date is less than 2000-01-01\"\n}";
		}

		if(end.isAfter(LocalDate.now())){
			return "{ \n\t\"Error\" : \"End date is after today's date\"\n}";
		}

		double count=0;
		while(!start.isEqual(end.plusDays(1))){
			count++;
			String jsonString;
			if(!cache.containsKey(start.toString())) {
				jsonString = getExchangeRate(start.toString());
				cache.put(start.toString(),jsonString);
			}else{
				jsonString = cache.get(start.toString());
			}

			Response r = g.fromJson(jsonString, Response.class);
			for(String cur:r.rates.keySet()){
				if(tm.containsKey(cur)) {
					tm.put(cur, tm.get(cur) + r.rates.get(cur));
				}
			}
			start=start.plusDays(1);
		}

		for(String cur:tm.keySet()){
			tm.put(cur,Math.round(tm.get(cur)/count * 10000D)/10000D);
		}

		return g.toJson(tm);
	}

	private static class Response{
		TreeMap<String,Double> rates;
		String base;
		String date;

		@Override
		public String toString() {
			return "Response " + rates ;
		}

	}

	private static String getExchangeRate(String date)
	{
		final String uri = "https://api.exchangeratesapi.io/"+date;
		RestTemplate restTemplate = new RestTemplate();
		String result= restTemplate.getForObject(uri, String.class);
		return result;
	}
}


