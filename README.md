Use The api with POST Request on /calculate 

Payload data should be in JSON like
```
{
    "range" : ["YYYY-MM-DD(StartDate)","YYYY-MM-DD(EndDate)"],
    "currencyList" : ["INR","USD"]
}
```
returns 

```
    {
        "INR": 79.40653913043491,
        "USD": 1.104747826086957
    }
```