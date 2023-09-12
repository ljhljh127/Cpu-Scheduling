import java.util.*;

class Data
{    /*inputData*/
    int ArrivalTime; //cpu 개별의 도착시간
    int NeedCpuTime; //cpu가 끝나기 까지의 필요한 시간
    int CpuBurstTime; //랜덤으로 주어지는 cpu burst의 값0~
    int IOBurstTime; //랜덤으로 주어지는 io burst의 값0~
    /*outputData*/
    int FinishingTime=0; //cpu개별의 끝난 시간과 모든 cpu끝난시간 필요
    int TurnaroundTime=0;// FinishingTime-도착시간
    int CpuTime=0; //cpu가 러닝 상태에 있던 시간 합치면 결국 processCpuTime
    int IOTime=0; //io상태에 있건 시간의 총합
    int WaitingTime=0; //ReadyQueue에 있던시간
    int CpuUtilization=0;
    int IOUtilization=0;
    int Throughout=0;//처리량 단위시간동안 몇개의 프로세스 끝났는지
    int ProcessIndex=0;
    //RandomData
    int qfcb=0;
    int qfib=0;


    public Data(int ArrivalTime,int NeedCpuTime,int CpuBurstTime,int IOBurstTime)
    {
        this.ArrivalTime = ArrivalTime;
        this.NeedCpuTime = NeedCpuTime;
        this.CpuBurstTime = CpuBurstTime;
        this.IOBurstTime = IOBurstTime;
    }

    public Data()
    {

    }



}
class SJF
{
    Comparator<Data> comparator = new Comparator<Data>() {
        public int compare(Data a, Data b) {
            return a.ArrivalTime - b.ArrivalTime;
        }

    };
    private Data[] p;
    private int ProcessCount;
    ArrayList<Data> ArrivalArrange = new ArrayList<Data>();
    public SJF(Data[] p, int ProcessCount) {

        this.p = p;
        this.ProcessCount = ProcessCount;
    }
    public void Arrivalsort() {
        for (int i = 0; i < ProcessCount; i++) {
            ArrivalArrange.add(p[i]);
            ArrivalArrange.get(i).ProcessIndex = i;
        }
        Collections.sort(ArrivalArrange, comparator);
    }

   ArrayList<Data> ReadyQueue = new ArrayList<>();
    Queue<Data> CpuQueue = new LinkedList<>();
    ArrayList<Data> IOQueue = new ArrayList<>();
    ArrayList<Data> FinishQueue = new ArrayList<>();
    int CpuQueueProgressTime = 0;//실 진행시간;
    Data queuefirst=null;

    public void ArrivaltoReadyQueue()
    {
        if(!ArrivalArrange.isEmpty())
        {
            for(int i=0;i<ArrivalArrange.size();i++)
            {
                if(ArrivalArrange.get(i).ArrivalTime==CpuQueueProgressTime )
                {
                   ArrivalArrange.get(i).qfcb=(int) (Math.random() * ArrivalArrange.get(i).CpuBurstTime) + 1;//랜덤화
                    if(ArrivalArrange.get(i).IOBurstTime==0)
                    {
                        ArrivalArrange.get(i).qfib=0;
                    }
                    else
                    {
                        ArrivalArrange.get(i).qfib = (int) (Math.random() * ArrivalArrange.get(i).IOBurstTime)+1 ;//랜덤화
                    }
                }
            }
            for(int i=0;i<ArrivalArrange.size();i++)
            {
                if(ArrivalArrange.get(0).ArrivalTime==CpuQueueProgressTime )
                {
                    ReadyQueue.add(ArrivalArrange.remove(i--));
                }
            }
        }
    }
    public void IoqueuetoenQueue()
    {
        if(!IOQueue.isEmpty())
        {
            for(int i=0;i<IOQueue.size();i++)
            {
                if(IOQueue.get(i).qfib<=0&&IOQueue.get(i).NeedCpuTime<=0)
                {
                    FinishQueue.add(IOQueue.remove(i--));
                }
            }



            for(int i=0;i<IOQueue.size();i++)
            {
                if(IOQueue.get(i).qfib<=0&&IOQueue.get(i).NeedCpuTime>0)
                {
                    IOQueue.get(i).qfcb=(int) (Math.random() *  IOQueue.get(i).CpuBurstTime) + 1;//랜덤화
                    if( IOQueue.get(i).IOBurstTime==0)
                    {
                        IOQueue.get(i).qfib=0;
                    }
                    else
                    {
                        IOQueue.get(i).qfib = (int) (Math.random() * IOQueue.get(i).IOBurstTime)+1 ;//랜덤화
                    }
                    ReadyQueue.add(IOQueue.remove(i));

                }
            }
        }
    }

    public void sjfRunning()
    {
        Comparator<Data> comparator2=new Comparator<Data>() {
            @Override
            public int compare(Data a, Data b)
            {
                return a.qfib-b.qfib;

            }
        };



        int x=0;
        while(true)
        {

            ArrivaltoReadyQueue();
            IoqueuetoenQueue();
            if(!CpuQueue.isEmpty())//cpu큐가 비어있지 않다면=프로세스가 스케줄링중이라면
            {
                if(queuefirst.NeedCpuTime<=0)//needCPu타임을 충족 했을때-->그 프로세스 스케줄링 종료
                {
                    queuefirst.FinishingTime=CpuQueueProgressTime;
                    queuefirst.TurnaroundTime=queuefirst.FinishingTime-queuefirst.ArrivalTime;
                    queuefirst.WaitingTime=queuefirst.TurnaroundTime-queuefirst.CpuTime-queuefirst.IOTime;
                    FinishQueue.add(CpuQueue.remove());//finish큐로 cpu가 나가서 cpu큐가 비어있게 되었을때
                    if(!ReadyQueue.isEmpty())
                    {
                        Collections.sort(ReadyQueue,comparator2);
                        CpuQueue.add(ReadyQueue.remove(0));
                        queuefirst=CpuQueue.peek();
                    }
                    else//레디큐가 비어있다면
                    {
                        //대기상태
                    }
                }
                else//프로세스가 스케줄링 중이며 needCputime을 충족하지 못했을때
                {
                    if(queuefirst.qfcb<=0)//qfcb 버스트가 0이되면
                    {
                        IOQueue.add(CpuQueue.remove());
                        IoqueuetoenQueue();
                        if(!ReadyQueue.isEmpty())
                        {
                            Collections.sort(ReadyQueue,comparator2);
                            CpuQueue.add(ReadyQueue.remove(0));
                            queuefirst=CpuQueue.peek();
                        }
                        else//레디큐가 비어있다면
                        {
                            //대기상태
                        }

                    }
                    else//버스트가 남은경우
                    {
                        //버스트 진행
                    }

                }



            }
            else//cpu 큐가 비어있다면
            {
                if(!ReadyQueue.isEmpty())//cpu 큐가 비어있고 레디큐에 프로세스 대기중이라면
                {
                    Collections.sort(ReadyQueue,comparator2);
                    CpuQueue.add(ReadyQueue.remove(0));
                    queuefirst=CpuQueue.peek();
                }
                else//Cpu큐가 비어있지만 레디큐에 프로세스가 대기중이지 아니라면
                {
                    if(IOQueue.isEmpty())
                    {
                        if(ArrivalArrange.isEmpty())
                        {
                            System.out.println("--------------------------------");
                            System.out.println("FcFs 알고리즘의 수행 결과입니다");
                            int biggest=0;
                            int Utilization=0;//CPU버스트 총합 구해서 계산
                            int AverageTurnAroundTime=0;
                            int AverageWaitingTime=0;
                            for(int i=0;i<FinishQueue.size();i++)
                            {
                                if(FinishQueue.get(i).FinishingTime>=biggest)
                                {
                                    biggest=FinishQueue.get(i).FinishingTime;
                                }
                                Utilization=Utilization+FinishQueue.get(i).CpuTime;
                                AverageTurnAroundTime=AverageTurnAroundTime+FinishQueue.get(i).TurnaroundTime;
                                AverageWaitingTime=AverageWaitingTime+FinishQueue.get(i).WaitingTime;



                                System.out.println("--------------------------------");
                                System.out.println("프로세스인덱스 "+FinishQueue.get(i).ProcessIndex);
                                System.out.println("FinishingTime is "+FinishQueue.get(i).FinishingTime);
                                System.out.println("TurnAroundTime is "+FinishQueue.get(i).TurnaroundTime);
                                System.out.println("CpuTime is "+FinishQueue.get(i).CpuTime);
                                System.out.println("IOTime is "+FinishQueue.get(i).IOTime);
                                System.out.println("WaitingTime is "+FinishQueue.get(i).WaitingTime);
                                System.out.println("--------------------------------");
                            }
                            System.out.println("SummaryData");
                            System.out.println("FinisningTime is "+biggest);
                            System.out.println("CPU UtiliZation is "+ (float)(Utilization*100)/biggest);
                            System.out.println("IOUtiliZation is "+(float)(x*100)/biggest);
                            System.out.println("AverageTurnAroundTime is "+ (float)AverageTurnAroundTime/FinishQueue.size());
                            System.out.println("AverageWaitingTime is "+ (float)(AverageWaitingTime/ FinishQueue.size()));
                            break;//스케줄링 종료됨

                        }
                        else
                        {

                            //아직 도착하지 않은 프로세스가 존재함 대기한다. 시간이 흐를동안

                        }
                    }
                    else//cpu큐 레디큐 비어있고 io큐는 비어있지 않음
                    {

                        //시간이 흘러 io큐에서 레디큐로 가기를 기다린다.

                    }
                }

            }

            if(!CpuQueue.isEmpty())
            {

                CpuQueue.peek().NeedCpuTime--;
                CpuQueue.peek().CpuTime++;
                CpuQueue.peek().qfcb--;
            }
            if(!IOQueue.isEmpty())
            {
                for (int i = 0; i < IOQueue.size(); i++)//io큐안에 있는 모든 qfib값 감소 IOTIme 값 증가
                {
                    IOQueue.get(i).qfib--;
                    IOQueue.get(i).IOTime++;
                }
            }
            if(!IOQueue.isEmpty())
            {
                x++;
            }

            CpuQueueProgressTime++;

        }



    }




}
public class SJFScheduling
{

    public static void main(String args[])
    {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Process Count");
        int ProcessCount=scanner.nextInt();
        Data[] data=new Data[ProcessCount+1];
        data[ProcessCount]=new Data();
        for(int i=0;i<data.length-1;i++)
        {
            data[i]=new Data(scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());
        }
        SJF sjf=new SJF(data,ProcessCount);
        sjf.Arrivalsort();
        sjf.sjfRunning();

    }


}
