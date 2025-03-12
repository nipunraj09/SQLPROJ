x=[1,2,3,4,5]

twos=9



for i,num in enumerate(x):
   if twos-num in x:
       
       print(twos-num,x.index(twos-num))
