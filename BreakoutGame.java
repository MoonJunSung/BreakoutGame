package s20404;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

public class BreakoutGame extends JPanel implements ActionListener, KeyListener, MouseListener {
    // 게임 화면 크기
    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    
    // 공 클래스
    class Ball {
        double x, y;
        double dx, dy;
        LinkedList<Point> trail;
        
        Ball(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.trail = new LinkedList<>();
        }
        
        void updateTrail() {
            trail.addLast(new Point((int)x, (int)y));
            if (trail.size() > TRAIL_LENGTH) {
                trail.removeFirst();
            }
        }
    }
    
    // 파티클 클래스
    class Particle {
        double x, y;
        double dx, dy;
        Color color;
        int life;
        
        Particle(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.dx = (Math.random() - 0.5) * 8;
            this.dy = (Math.random() - 0.5) * 8;
            this.life = 30;
        }
    }
    
    // 공 속성
    ArrayList<Ball> balls;
    static final int BALL_SIZE = 15;
    static final int TRAIL_LENGTH = 15;
    
    // 패들 속성
    int paddleX = 350;
    static final int PADDLE_Y = 550;
    static final int PADDLE_WIDTH = 100;
    static final int PADDLE_HEIGHT = 15;
    static final int PADDLE_SPEED = 10;
    
    // 블럭 속성
    static final int BRICK_ROWS = 6;
    static final int BRICK_COLS = 10;
    static final int BRICK_WIDTH = 65;
    static final int BRICK_HEIGHT = 25;
    static final int BRICK_PADDING = 8;
    ArrayList<Brick> bricks;
    
    // 파워업 및 파티클
    ArrayList<PowerUp> powerUps;
    ArrayList<Particle> particles;
    
    // 게임 상태
    boolean gameRunning = false;
    boolean gameStarted = false;
    int score = 0;
    int lives = 3;
    Timer timer;
    
    // 키 입력 상태
    boolean leftPressed = false;
    boolean rightPressed = false;
    
    // 시작 버튼 영역
    Rectangle startButton;
    
    // 블럭 클래스
    class Brick {
        int x, y;
        boolean isVisible;
        Color color;
        
        Brick(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.isVisible = true;
        }
    }
    
    // 파워업 클래스
    class PowerUp {
        int x, y;
        int dy = 2;
        static final int SIZE = 25;
        
        PowerUp(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    public BreakoutGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        
        balls = new ArrayList<>();
        powerUps = new ArrayList<>();
        particles = new ArrayList<>();
        startButton = new Rectangle(WIDTH/2 - 50, HEIGHT/2 - 50, 100, 100);
        
        initGame();
        
        timer = new Timer(10, this);
        timer.start();
    }
    
    void initGame() {
        balls.clear();
        balls.add(new Ball(400, 350, 3, -3));
        powerUps.clear();
        particles.clear();
        initBricks();
    }
    
    void initBricks() {
        bricks = new ArrayList<>();
        Color[] colors = {
            new Color(255, 100, 120),  // 빨강
            new Color(255, 160, 100),  // 주황
            new Color(255, 220, 100),  // 노랑
            new Color(150, 255, 150),  // 연두
            new Color(100, 200, 255),  // 하늘
            new Color(200, 150, 255)   // 보라
        };
        
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLS; col++) {
                int x = col * (BRICK_WIDTH + BRICK_PADDING) + 40;
                int y = row * (BRICK_HEIGHT + BRICK_PADDING) + 60;
                bricks.add(new Brick(x, y, colors[row % colors.length]));
            }
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 그라데이션 배경
        GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 40), 
                                                   0, HEIGHT, new Color(40, 40, 80));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        if (!gameStarted) {
            // 시작 화면
            g2d.setColor(new Color(50, 255, 100));
            g2d.fillOval(startButton.x, startButton.y, startButton.width, startButton.height);
            
            // 버튼 광택 효과
            g2d.setPaint(new GradientPaint(startButton.x, startButton.y, new Color(100, 255, 150, 150),
                                          startButton.x, startButton.y + 50, new Color(50, 255, 100, 50)));
            g2d.fillOval(startButton.x + 10, startButton.y + 10, 80, 40);
            
            // 재생 버튼 아이콘
            int[] xPoints = {startButton.x + 35, startButton.x + 35, startButton.x + 65};
            int[] yPoints = {startButton.y + 25, startButton.y + 75, startButton.y + 50};
            g2d.setColor(Color.WHITE);
            g2d.fillPolygon(xPoints, yPoints, 3);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 35));
            String title = "블럭 깨기";
            FontMetrics fm = g2d.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, (WIDTH - titleWidth) / 2, HEIGHT / 2 - 100);
            
            g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            g2d.setColor(new Color(200, 200, 200));
            String instruction = "시작하려면 재생 버튼을 클릭하세요";
            int instWidth = g2d.getFontMetrics().stringWidth(instruction);
            g2d.drawString(instruction, (WIDTH - instWidth) / 2, HEIGHT / 2 + 80);
        } else if (gameRunning) {
            // 파티클 그리기
            for (Particle particle : particles) {
                g2d.setColor(new Color(particle.color.getRed(), particle.color.getGreen(), 
                                     particle.color.getBlue(), particle.life * 8));
                g2d.fillRect((int)particle.x, (int)particle.y, 4, 4);
            }
            
            // 모든 공 그리기
            for (Ball ball : balls) {
                // 공 궤적 그리기
                int trailSize = ball.trail.size();
                for (int i = 0; i < trailSize; i++) {
                    Point p = ball.trail.get(i);
                    int alpha = (int)(200 * (i / (float)TRAIL_LENGTH));
                    g2d.setColor(new Color(100, 200, 255, alpha));
                    int size = BALL_SIZE * (i + 5) / TRAIL_LENGTH;
                    g2d.fillOval(p.x - size/2, p.y - size/2, size, size);
                }
                
                // 공 그리기 (그라데이션)
                g2d.setPaint(new RadialGradientPaint(new Point2D.Float((int)ball.x, (int)ball.y), 
                    BALL_SIZE/2, new float[]{0f, 1f}, 
                    new Color[]{new Color(150, 220, 255), new Color(50, 150, 255)}));
                g2d.fillOval((int)ball.x - BALL_SIZE/2, (int)ball.y - BALL_SIZE/2, BALL_SIZE, BALL_SIZE);
            }
            
            // 패들 그리기 (그라데이션과 광택 효과)
            g2d.setPaint(new GradientPaint(paddleX, PADDLE_Y, new Color(200, 200, 200),
                                          paddleX, PADDLE_Y + PADDLE_HEIGHT, new Color(100, 100, 100)));
            g2d.fillRoundRect(paddleX, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT, 10, 10);
            
            // 패들 광택
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.fillRoundRect(paddleX + 10, PADDLE_Y + 2, PADDLE_WIDTH - 20, 5, 5, 5);
            
            // 블럭 그리기
            for (Brick brick : bricks) {
                if (brick.isVisible) {
                    // 블럭 그림자
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillRoundRect(brick.x + 2, brick.y + 2, BRICK_WIDTH, BRICK_HEIGHT, 5, 5);
                    
                    // 블럭 본체
                    g2d.setColor(brick.color);
                    g2d.fillRoundRect(brick.x, brick.y, BRICK_WIDTH, BRICK_HEIGHT, 5, 5);
                    
                    // 블럭 광택
                    g2d.setPaint(new GradientPaint(brick.x, brick.y, 
                        new Color(255, 255, 255, 100), brick.x, brick.y + BRICK_HEIGHT/2, 
                        new Color(255, 255, 255, 0)));
                    g2d.fillRoundRect(brick.x, brick.y, BRICK_WIDTH, BRICK_HEIGHT/2, 5, 5);
                    
                    // 블럭 테두리
                    g2d.setColor(brick.color.darker());
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(brick.x, brick.y, BRICK_WIDTH, BRICK_HEIGHT, 5, 5);
                }
            }
            
            // 파워업 그리기
            for (PowerUp powerUp : powerUps) {
                // 파워업 광선 효과
                g2d.setColor(new Color(50, 255, 50, 50));
                g2d.fillOval(powerUp.x - 5, powerUp.y - 5, PowerUp.SIZE + 10, PowerUp.SIZE + 10);
                
                g2d.setColor(new Color(50, 255, 50));
                g2d.fillOval(powerUp.x, powerUp.y, PowerUp.SIZE, PowerUp.SIZE);
                
                g2d.setColor(Color.WHITE);
                g2d.fillOval(powerUp.x + 5, powerUp.y + 5, PowerUp.SIZE - 10, PowerUp.SIZE - 10);
                
                g2d.setColor(new Color(50, 255, 50));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(powerUp.x + 12, powerUp.y + 8, powerUp.x + 12, powerUp.y + 17);
                g2d.drawLine(powerUp.x + 8, powerUp.y + 12, powerUp.x + 17, powerUp.y + 12);
            }
            
            // UI 패널 배경
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, WIDTH, 50);
            
            // 점수와 생명 표시
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 20));
            g2d.drawString("점수: " + score, 20, 30);
            g2d.drawString("생명: ", WIDTH - 150, 30);
            
            // 하트로 생명 표시
            for (int i = 0; i < lives; i++) {
                g2d.setColor(new Color(255, 100, 100));
                int heartX = WIDTH - 90 + i * 25;
                g2d.fillOval(heartX, 15, 10, 10);
                g2d.fillOval(heartX + 8, 15, 10, 10);
                int[] xPoints = {heartX, heartX + 18, heartX + 9};
                int[] yPoints = {22, 22, 35};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
            
            g2d.setColor(Color.WHITE);
            g2d.drawString("공: " + balls.size(), WIDTH/2 - 30, 30);
        } else {
            // 게임 오버 화면
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 45));
            String message = lives <= 0 ? "게임 오버!" : "축하합니다!";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(message);
            g2d.drawString(message, (WIDTH - msgWidth) / 2, HEIGHT / 2);
            
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 25));
            String scoreMsg = "최종 점수: " + score;
            int scoreWidth = g2d.getFontMetrics().stringWidth(scoreMsg);
            g2d.drawString(scoreMsg, (WIDTH - scoreWidth) / 2, HEIGHT / 2 + 50);
            
            g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            g2d.setColor(new Color(200, 200, 200));
            String restartMsg = "재시작: SPACE  |  시작 화면: ESC";
            int restartWidth = g2d.getFontMetrics().stringWidth(restartMsg);
            g2d.drawString(restartMsg, (WIDTH - restartWidth) / 2, HEIGHT / 2 + 90);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameRunning && gameStarted) {
            // 패들 이동
            if (leftPressed && paddleX > 0) {
                paddleX -= PADDLE_SPEED;
            }
            if (rightPressed && paddleX < WIDTH - PADDLE_WIDTH) {
                paddleX += PADDLE_SPEED;
            }
            
            // 새로 추가할 공들을 저장할 임시 리스트
            ArrayList<Ball> newBalls = new ArrayList<>();
            
            // 모든 공 업데이트
            Iterator<Ball> ballIterator = balls.iterator();
            while (ballIterator.hasNext()) {
                Ball ball = ballIterator.next();
                
                // 공 궤적 업데이트
                ball.updateTrail();
                
                // 공 이동
                ball.x += ball.dx;
                ball.y += ball.dy;
                
                // 벽 충돌 검사
                if (ball.x <= BALL_SIZE/2) {
                    ball.x = BALL_SIZE/2;
                    ball.dx = Math.abs(ball.dx);
                }
                if (ball.x >= WIDTH - BALL_SIZE/2) {
                    ball.x = WIDTH - BALL_SIZE/2;
                    ball.dx = -Math.abs(ball.dx);
                }
                if (ball.y <= BALL_SIZE/2) {
                    ball.y = BALL_SIZE/2;
                    ball.dy = Math.abs(ball.dy);
                }
                
                // 바닥 충돌
                if (ball.y >= HEIGHT) {
                    ballIterator.remove();
                    continue;
                }
                
                // 패들 충돌 검사
                Rectangle ballRect = new Rectangle((int)ball.x - BALL_SIZE/2, (int)ball.y - BALL_SIZE/2, BALL_SIZE, BALL_SIZE);
                Rectangle paddleRect = new Rectangle(paddleX, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT);
                
                if (ballRect.intersects(paddleRect) && ball.dy > 0) {
                    ball.dy = -Math.abs(ball.dy);
                    int hitPos = (int)ball.x - paddleX;
                    ball.dx = (hitPos - PADDLE_WIDTH/2) / 8.0;
                    
                    // 속도 제한
                    if (Math.abs(ball.dx) > 6) {
                        ball.dx = ball.dx > 0 ? 6 : -6;
                    }
                }
                
                // 블럭 충돌 검사
                for (Brick brick : bricks) {
                    if (brick.isVisible) {
                        Rectangle brickRect = new Rectangle(brick.x, brick.y, BRICK_WIDTH, BRICK_HEIGHT);
                        if (ballRect.intersects(brickRect)) {
                            brick.isVisible = false;
                            
                            // 충돌 방향 계산
                            double ballCenterX = ball.x;
                            double ballCenterY = ball.y;
                            double brickCenterX = brick.x + BRICK_WIDTH / 2.0;
                            double brickCenterY = brick.y + BRICK_HEIGHT / 2.0;
                            
                            double dx = ballCenterX - brickCenterX;
                            double dy = ballCenterY - brickCenterY;
                            
                            // 어느 면에서 충돌했는지 판단
                            if (Math.abs(dx / BRICK_WIDTH) > Math.abs(dy / BRICK_HEIGHT)) {
                                ball.dx = -ball.dx;
                            } else {
                                ball.dy = -ball.dy;
                            }
                            
                            // 공이 블럭에 끼지 않도록 위치 조정
                            if (ball.y < brick.y) {
                                ball.y = brick.y - BALL_SIZE/2 - 1;
                            } else if (ball.y > brick.y + BRICK_HEIGHT) {
                                ball.y = brick.y + BRICK_HEIGHT + BALL_SIZE/2 + 1;
                            }
                            
                            score += 10;
                            
                            // 파티클 생성
                            for (int i = 0; i < 10; i++) {
                                particles.add(new Particle(brick.x + BRICK_WIDTH/2, 
                                                         brick.y + BRICK_HEIGHT/2, brick.color));
                            }
                            
                            // 파워업 생성 (15% 확률)
                            if (Math.random() < 0.15) {
                                powerUps.add(new PowerUp(brick.x + BRICK_WIDTH/2 - PowerUp.SIZE/2, brick.y));
                            }
                            
                            // 모든 블럭을 깼는지 확인
                            if (checkWin()) {
                                gameRunning = false;
                            }
                            break;
                        }
                    }
                }
            }
            
            // 파티클 업데이트
            Iterator<Particle> particleIterator = particles.iterator();
            while (particleIterator.hasNext()) {
                Particle particle = particleIterator.next();
                particle.x += particle.dx;
                particle.y += particle.dy;
                particle.dy += 0.3; // 중력
                particle.life--;
                if (particle.life <= 0) {
                    particleIterator.remove();
                }
            }
            
            // 파워업 업데이트
            Iterator<PowerUp> powerUpIterator = powerUps.iterator();
            while (powerUpIterator.hasNext()) {
                PowerUp powerUp = powerUpIterator.next();
                powerUp.y += powerUp.dy;
                
                Rectangle powerUpRect = new Rectangle(powerUp.x, powerUp.y, PowerUp.SIZE, PowerUp.SIZE);
                Rectangle paddleRect = new Rectangle(paddleX, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT);
                
                if (powerUpRect.intersects(paddleRect)) {
                    // 멀티볼 파워업 - 새로운 공을 임시 리스트에 추가
                    if (!balls.isEmpty()) {
                        Ball firstBall = balls.get(0);
                        double newDx = firstBall.dx + (Math.random() - 0.5) * 2;
                        double newDy = -Math.abs(firstBall.dy);
                        newBalls.add(new Ball(firstBall.x, firstBall.y, newDx, newDy));
                    }
                    score += 30;
                    powerUpIterator.remove();
                } else if (powerUp.y > HEIGHT) {
                    powerUpIterator.remove();
                }
            }
            
            // Iterator 순회가 끝난 후 새로운 공들을 추가
            balls.addAll(newBalls);
            
            // 모든 공이 떨어졌는지 확인
            if (balls.isEmpty()) {
                lives--;
                if (lives <= 0) {
                    gameRunning = false;
                } else {
                    resetBalls();
                }
            }
        }
        
        repaint();
    }
    
    void resetBalls() {
        balls.clear();
        balls.add(new Ball(400, 350, 3, -3));
    }
    
    boolean checkWin() {
        for (Brick brick : bricks) {
            if (brick.isVisible) {
                return false;
            }
        }
        return true;
    }
    
    void restartGame() {
        gameRunning = true;
        gameStarted = true;
        score = 0;
        lives = 3;
        paddleX = 350;
        initGame();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_SPACE && !gameRunning && gameStarted) {
            restartGame();
        }
        if (key == KeyEvent.VK_ESCAPE && !gameRunning) {
            gameStarted = false;
            gameRunning = false;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // 마우스 이벤트
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameStarted && startButton.contains(e.getPoint())) {
            restartGame();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        // 한글 폰트 렌더링 개선
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        JFrame frame = new JFrame("블럭 깨기 게임");
        BreakoutGame game = new BreakoutGame();
        
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}