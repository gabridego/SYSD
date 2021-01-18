list.txt:	list1.txt list2.txt list3.txt
	cp list1.txt list.txt ; cat list2.txt >> list.txt ; cat list3.txt >> list.txt ;

premier:
	gcc premier.c -o premier -lm

list1.txt: premier
	./premier 2 `echo 1*200000000/20-1 |bc` > list1.txt

list2.txt: premier
	./premier `echo 1*200000000/20 |bc` `echo 2*200000000/20-1 |bc` > list2.txt

list3.txt: premier
	./premier `echo 2*200000000/20 |bc` `echo 3*200000000/20-1 |bc` > list3.txt
