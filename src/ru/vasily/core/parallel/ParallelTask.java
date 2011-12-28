package ru.vasily.core.parallel;

public interface ParallelTask
{
	void doPart(double start, double end);// TODO refactor 
/*

doPart(Partitioner par){

for(Integer i : par.range(1,100))
{
	.......
}
*/

}
