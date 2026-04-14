import {Camera, CameraResultType, CameraSource} from '@capacitor/camera';
import {sendMessage} from "@/api/message.js";
import { useUserStore } from '@/store/user'
import {createConversation, getConversationList} from "@/api/conversation.js";
import router from "@/router/index.js";
import { showToast } from "vant";

const userStore = useUserStore()

export async function takePicture(options = {}) {
    const {
        source = 'prompt', // 默认让用户选择
        quality = 95,
        allowEditing = true,
    } = options;

    try {
        // 构建配置对象
        const cameraOptions = {
            resultType: CameraResultType.Uri, // 推荐使用 Uri，性能更好，适合直接显示在 img 标签
            source: source === 'prompt' ? undefined : (source === 'camera' ? CameraSource.Camera : CameraSource.Photos),
            quality: quality,
            allowEditing: allowEditing,
            webUseInput: true, // 关键：在 PWA/Web 端调用原生文件选择器
        };

        // 如果用户选择 'prompt'，Capacitor 会自动处理原生弹窗 (ActionSheet)
        // 如果是在 Web 浏览器且没有原生环境，webUseInput: true 会触发 input type='file'

        const image = await Camera.getPhoto(cameraOptions);

        // image.webPath 可以直接用于 <img src={image.webPath} />
        console.log('照片获取成功:', image.webPath);

        return image;

    } catch (error) {
        // 用户取消操作通常会抛出 "User cancelled" 错误，可以根据需要静默处理
        if (error.message && error.message.includes('cancel')) {
            console.log('用户取消了操作');
            return null;
        }

        console.error('拍照或获取图片失败:', error);
        throw error; // 或者返回 null，取决于你的业务逻辑
    }
}

/**
 * 将 Capacitor Camera 返回的图片对象转换为 Base64 字符串
 * @param photo Camera.getPhoto() 返回的 image 对象
 * @returns Promise<string> 图片的 base64（不带 data:image/xxx;base64, 前缀）
 */

/**
 * 将 Capacitor Camera 返回的图片对象转换为 Base64 字符串
 * @param photo Camera.getPhoto() 返回的 image 对象
 * @returns Promise<string> 图片的 base64（不带前缀）
 */
export async function convertImageToBase64(photo) {
    try {
        // 🚨 调试：检查输入参数
        console.log('[convertImageToBase64] 开始转换，photo:', photo);
        console.log('[convertImageToBase64] photo.webPath:', photo?.webPath);
        
        if (!photo || !photo.webPath) {
            console.error('[convertImageToBase64] 参数无效：photo 或 photo.webPath 为空');
            return null;
        }

        // 🚨 调试：检查webPath是否有效
        console.log('[convertImageToBase64] webPath:', photo.webPath);
        
        const response = await fetch(photo.webPath);
        
        // 🚨 调试：检查fetch响应
        console.log('[convertImageToBase64] fetch 响应状态:', response.status);
        console.log('[convertImageToBase64] fetch 响应类型:', response.type);
        
        if (!response.ok) {
            console.error('[convertImageToBase64] fetch 请求失败:', response.status, response.statusText);
            return null;
        }

        const blob = await response.blob();
        
        // 🚨 调试：检查blob
        console.log('[convertImageToBase64] blob 大小:', blob?.size);
        console.log('[convertImageToBase64] blob 类型:', blob?.type);
        
        return new Promise((resolve, reject) => {
            const reader = new FileReader();

            reader.onloadend = () => {
                // 🚨 调试：检查reader结果
                console.log('[convertImageToBase64] FileReader 结果:', reader.result);
                
                const result = reader.result;
                if (typeof result === 'string') {
                    // 🚨 调试：检查base64结果
                    console.log('[convertImageToBase64] Base64 转换成功，长度:', result.length);
                    resolve(result);
                } else {
                    console.error('[convertImageToBase64] 转换结果不是字符串:', typeof result);
                    reject('转换失败：结果不是字符串');
                }
            };

            reader.onerror = (error) => {
                console.error('[convertImageToBase64] FileReader 错误:', error);
                reject('转换失败：FileReader 错误');
            };
            
            reader.onabort = () => {
                console.error('[convertImageToBase64] FileReader 被中止');
                reject('转换失败：FileReader 被中止');
            };

            console.log('[convertImageToBase64] 开始读取 blob 为 DataURL');
            reader.readAsDataURL(blob);
        });
    } catch (err) {
        console.error('[convertImageToBase64] 转换异常:', err);
        console.error('[convertImageToBase64] 错误详情:', {
            message: err?.message,
            stack: err?.stack,
            name: err?.name
        });
        return null;
    }
}


async function getOrCreateReportAnalysisConversation() {
    try {
        // 生成唯一的对话标题（包含餐型、食物名称和时间）
        const Label = "报告分析"
        const timestamp = new Date().toLocaleString('zh-CN', {
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        })
        const conversationTitle = `${Label} - ${timestamp}`

        // 每次都创建新会话
        console.log('[ReportAnalysis] 创建新的对话:', conversationTitle)
        const createRes = await createConversation(userStore.userId, conversationTitle)
        console.log('[ReportAnalysis] 新会话创建成功:', createRes.data.id)
        return createRes.data.id
    } catch (e) {
        console.error('[ReportAnalysis] 创建会话失败:', e)
        throw new Error('无法获取会话')
    }
}

export async function sendReport() {
    try {
        console.log('[ReportAnalysis] 开始报告分析流程')
        
        // 1. 拍照或选择图片
        const image = await takePicture({ source: 'prompt' });
        
        if (!image) {
            console.log('[ReportAnalysis] 用户取消了图片选择');
            return;
        }

        // 2. 转换图片为base64
        console.log('[ReportAnalysis] 开始转换图片为base64');
        const base64 = await convertImageToBase64(image);
        
        if (!base64) {
            console.error('[ReportAnalysis] 图片转换失败，base64为空');
            showToast('图片转换失败，请重试');
            return;
        }

        console.log('[ReportAnalysis] 图片转换成功，base64长度:', base64.length);

        // 3. 创建新对话
        console.log('[ReportAnalysis] 准备创建新对话');
        const conversationId = await getOrCreateReportAnalysisConversation();
        console.log('[ReportAnalysis] 新会话创建成功，ID:', conversationId);

        // 4. 发送报告分析请求
        console.log('[ReportAnalysis] 发送API请求');
        const res = await sendMessage({
            userId: userStore.userId,
            function_type: "report_analysis",
            conversationId: conversationId,
            img: base64,
            content: "报告分析",
            role: "user",
            mimeType: ''
        });
        console.log('[ReportAnalysis] API 响应:', res);

        // 5. 跳转到主页面并刷新
        console.log('[ReportAnalysis] 跳转到主页面，conversationId:', conversationId);
        await router.push({
            name: 'Home',
            query: { conversationId }
        });

        // 6. 刷新主页面（通过路由参数变化触发）
        console.log('[ReportAnalysis] 主页面已刷新，等待AI回复');
        
        // 7. 显示成功提示
        showToast('分析完成，正在等待回复...');

    } catch (err) {
        console.error('[ReportAnalysis] 上传失败:', err);
        console.error('[ReportAnalysis] 错误详情:', {
            message: err?.message,
            stack: err?.stack,
            response: err?.response?.data,
            status: err?.response?.status
        });
        showToast('分析失败，请重试');
    }
}
