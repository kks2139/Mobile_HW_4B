package com.example.maze;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import java.util.Stack;
/**
 * Created by 광선 on 2016-05-26.
 * 실행시 미로가 랜덤하게 생성된다
 * 맵위에 캐릭터를 터치했을때만 이동이 가능하고 이동한 자취선이 그려진다
 * 만약 벽에 닿으면 다시 처음부터 시작된다
 * 도착지점에 도달했을때도 맵이 랜덤생성되면서 다시 시작된다
 */
public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {

    boolean isWall = false, isEnd=false;
    int rand,x=0,y=0;
    int[][] position = new int[25][18];// 벽과 길을 구분해줄 배열

    int tx,ty,p_tx,p_ty,stack=1;
    int lastX, lastY, currX, currY;
    boolean isDeleting;
    Canvas cacheCanvas,c;
    Bitmap backBuffer, bunny,blu,pnk,end;
    int width, height, clientHeight;
    Paint paint,basic,dot,blue;

    Context context;
    SurfaceHolder mHolder;// 캔버스에 그릴때 사용

    // 벽을 그려주기 위한 변수들 선언 //
    Paint wallp = new Paint();

    public DrawingSurface(Context context) {//생성자
        super(context);
        this.context = context;
        init();
    }

    public DrawingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {// 대부분의 변수들을 여기서 초기화 해준다

        lastX = lastY = currX = currY = 130;// 현재와 이전 좌표를 시작점인 130으로 모두 초기화 해준다
        mHolder = getHolder();// surface holder를 얻어옴
        mHolder.addCallback(this);// 여기서 사용할 것이다

        blue = new Paint();
        blue.setColor(Color.BLUE);
        blue.setStrokeWidth(80);

        for(int k=0;k<25;k++){// 미로를 랜덤하게 그려준다
            for(int i=0;i<18;i++){
                rand = (int)(Math.random()*2);// 1, 0 중 랜덤하게 숫자를 받아서 이차원배열에 저장하고 1이면 벽 0이면 길로 표시해줄거다
                Log.d("Test", "rand == "+rand);

                if(k==0 || i==0 || k==24 || i==17) {// 테두리 부분은 일부빼고 다 칠해준다

                        position[k][i] = 1;// 테두리는 다 칠해준다
                }
                else {// 테두리 아니면 걍 랜덤하게 그린다
                    //if(k==10&&i>10)//테스트
                        //position[k][i] = 1;// 테스트
                    if (rand == 1) // 여기부분 다 살려야함~~~
                        position[k][i] = 1;
                    else
                        position[k][i] = 0;

                    if(k==1&&i==1)
                        position[k][i] = 0;
                    if(k==23&&i==16)
                        position[k][i] = 0;

                }
            }// 안쪽 for문

        }// 밖같 for문

        basic = new Paint();

        dot = new Paint();
        dot.setStrokeWidth(80);
        dot.setColor(Color.CYAN);

        bunny = BitmapFactory.decodeResource(getResources(), R.drawable.bunny);
        bunny = Bitmap.createScaledBitmap(bunny, 140, 140, false);
        blu = BitmapFactory.decodeResource(getResources(), R.drawable.blue);
        blu = Bitmap.createScaledBitmap(blu, 50, 50, false);
        pnk = BitmapFactory.decodeResource(getResources(), R.drawable.pink);
        pnk = Bitmap.createScaledBitmap(pnk, 50, 50, false);

        MazePath mp = new MazePath();// 미로길찾기 클래스변수 생성
        while(true) {// 랜덤하게 숫자를 넣으면 길이 없을 수 도 있다. 그래서 길을 찾을때까지 무한루프를 돌려준다
            if(mp.MazePath1()== -1) {// 만약 못찾으면 다시실행

                Log.d("sss", "시~");

                for(int k=0;k<25;k++){// 미로를 랜덤하게 그려준다
                    for(int i=0;i<18;i++){
                        rand = (int)(Math.random()*2);
                       // Log.d("Test", "rand == "+rand);

                        //-------- 미로의 경로가 존재하지않으면 다시 그려준다 -------//
                        if(k==0 || i==0 || k==24 || i==17) {// 테두리 부분은 일부빼고 다 칠해준다
                            if(k==23&&i==17)
                                position[k][i] = 0;// 끝점 위
                            else
                                position[k][i] = 1;// 테두리 다 칠함
                        }
                        else {// 아니면 걍 랜덤하게 그린다

                            if (rand == 1)
                                position[k][i] = 1;
                            else
                                position[k][i] = 0;

                            if(k==1&&i==1)
                                position[k][i] = 0;
                            if(k==23&&i==16)
                                position[k][i] = 0;

                        }
                    }// 안쪽 for문

                }// 밖같 for문
            }
            else
                break;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {// 화면이 바뀔때 처리

    }

    public void surfaceCreated(SurfaceHolder holder) {// 화면 이 그려질때 처리

        // 그리기에 필요한 변수들 선언
        width = getWidth();
        height = getHeight();
        Log.d("TAG", "가로: "+width+" 세로: "+height);
        cacheCanvas = new Canvas();
        backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //back buffer
        cacheCanvas.setBitmap(backBuffer);
        cacheCanvas.drawColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        wallp.setColor(Color.BLACK);

        doDraw();// 그려주는 함수 호출
    }

    public void surfaceDestroyed(SurfaceHolder holder) {// 화면이 끝날때 처리

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {// 터치된 상태에따라 어떻게 할건지 정해준다
        super.onTouchEvent(event);
        int action = event.getAction();

        // 화면을 아무데나 찍는다고 거기서 그려지는게 아니라
        // 현재 좌표가 방금 그려진 선의 점의 일정 영역 안에 있을때만 선을 이어서 그릴 수 있다
        if( (int)event.getX()>currX-70 && (int)event.getX()<currX+70 &&
                (int)event.getY()>currY-70 && (int)event.getY()<currY+70) {

            switch (action) {
                case MotionEvent.ACTION_MOVE:// 이동하는중에 계속해서 불린다
                    if (isDeleting) break;
                    tx = (int) event.getX();// 현재 이동하는중에 받아온 새 좌표
                    ty = (int) event.getY();
                    p_tx = tx;
                    p_ty = ty;
                    if(isWall == false){// 벽이 아니라면 걍 지금 얻은 새 좌표 사용
                    currX = tx;
                    currY = ty;
                    }else if(isWall == true){// 벽닿았나 확인

                        Log.d("333", "움직이는즁에 벽이면..");
                        return true;
                    }

                    if(currX>=1360 && currX<=1440 && currY>=1840 && currY<=1920){// 현재 좌표가 도착지점 영역에 닿으면
                        Log.d("end", "도착~~~!!");
                        init();// 다시 변수값들 초기화 해주고
                        surfaceCreated(mHolder);// 화면에 다시 그려준다
                        return true;
                    }

                    for(int k=0;k<25;k++) {// 이차원배열을 다 확인해서 1이면(벽이면) 그영역을 지정해줘서 캐릭터가 벽에 닿았을시 다시 처음부터 시작되게한다
                        for (int i = 0; i < 18; i++) {

                            if(position[k][i] == 1){// 벽일 때

                                if(i*80<=currX && i*80+80>=currX && k*80<=currY && k*80+80>=currY){// 벽들을 다 검사해서 지금 좌표와 맞닿아 있는지를 판별
                                    Log.d("111", "벽이면~~");
                                    init();// 그러면 다시 초기화하고
                                    surfaceCreated(mHolder);// 다시 그려준다
                                    return true;
                                }
                                else{
                                    isWall = false;
                                }
                            }
                        }
                    }
                        cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);// 이동선을 그려준다
                        lastX = currX;// 현좌표는 전좌표가 된다
                        lastY = currY;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isDeleting) isDeleting = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    cacheCanvas.drawColor(Color.WHITE);
                    isDeleting = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
        }
        doDraw();// 터치마다 다시 그려준다

        return true;
    }

    public void doDraw() {// 실질적으로 여기서 벽이랑 캐릭터랑 이동선이랑 다 그려준다


        Canvas canvas;
        if (clientHeight == 0) {
            clientHeight = getClientHeight();
            height = clientHeight;
            backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas.setBitmap(backBuffer);
            cacheCanvas.drawColor(Color.WHITE);
        }
        canvas = mHolder.lockCanvas(null);// 캔버스에 그리기를 허용

            canvas.drawBitmap(backBuffer, 0, 0, paint);
            canvas.drawBitmap(bunny, currX - 70, currY - 70, basic);// 선을 따라가는 캐릭터
            canvas.drawBitmap(blu, 1380, 1860, basic);// 도착지점 표시
            canvas.drawBitmap(pnk, 90, 90, basic);// 시작지점 표시

            for (int k = 0; k < 25; k++) {// 배열을 확인해서 일정 영역을 정해줘서 화면에 그려준다
               for (int i = 0; i < 18; i++) {
                    if (position[k][i] == 3)
                        canvas.drawPoint(i * 80 + 40, k * 80 + 40, blue);
                    if (position[k][i] == 1)
                        canvas.drawPoint(i * 80 + 40, k * 80 + 40, dot);
                }
        }
        mHolder.unlockCanvasAndPost(canvas);// 그리기를 끝낸다
    }
    private int getClientHeight() {
        Rect rect = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;
        return ((Activity) context).getWindowManager().getDefaultDisplay().
                getHeight() - statusBarHeight - titleBarHeight;
    }


    // --------------------------------- 미로생성 판별 --------------------------//
    class MazeCell{
        int i;
        int j;
        int dir;
        public MazeCell(int _i,int _j,int _dir){
            i = _i;
            j = _j;
            dir = _dir;
        }
        public String toString(){
            return "<" + i  + "," + j + ">";
        }
    }

    class MazePath {

        int num=0;

        public int getNum()
        {
            num=1;
            return num;
        }

        public int MazePath1(){  //알고리즘대로 프로그램 작성

            int flag=0;
            int[][] maze = new int[25][18];
            maze = position;

            int[][] move = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}}; //북 동 남 서
            int m = maze.length -2;
            int n = maze[0].length -1;

            int mark[][] = new int[maze.length][maze[0].length];
            Stack st = new Stack();
            Stack st1 = new Stack();  //올바른 경로 삽입할 스택
            st.push(new MazeCell(1, 1, 1));  //초기출발위치맟 방향 설정
            while(st.isEmpty()!=true){
                MazeCell mc = (MazeCell)st.pop();  //지나온 경로를 pop함
                while(mc.dir<=3){
                    int nextI = mc.i + move[mc.dir][0];  //갈려고하는 방향으로 i+
                    int nextJ = mc.j + move[mc.dir][1];  //갈려고하는 방향으로 j+

                    if(nextI == m && nextJ ==n){  //미로경로 발견
                        //            System.out.println("경로를 발견했습니다");
                        Log.d("find", "경로를 발견했다네");
                        st.push(new MazeCell(mc.i,mc.j,mc.dir)); //마지막 경로 지정
                        st.push(new MazeCell(nextI,nextJ,0));    //마지막 지점 지정
                        while(st.isEmpty()!=true){
                            st1.push((MazeCell)st.pop());
                        }
                        while(st1.isEmpty()!=true){
                            mc = (MazeCell)st1.pop(); //올바른 경로 출력
                            //System.out.println(mc);
                            maze[mc.i][mc.j]=2;
                        }
                        flag=getNum();
                        return 7;
                    }
                    if(maze[nextI][nextJ]==0 && mark[nextI][nextJ]==0){//이동가능&시도해보지 않은위치
                        mark[nextI][nextJ] = 1;
                        st.push(new MazeCell(mc.i,mc.j,mc.dir));  //지나온경로를 스택에 push
                        //mc = new MazeCell(nextI,nextJ,0);  //새로운 위치와 방향 설정 방향은 초기화
                        mc.i = nextI;
                        mc.j = nextJ;
                        mc.dir = 0;
                    }
                    else
                    {
                        mc.dir++;
                    }
                }
            }
            //  System.out.println("경로를 발견하지 못했습니다");
            Log.d("fail", "경로가 없어ㅠ ");
            return -1;
        }
    }

}




