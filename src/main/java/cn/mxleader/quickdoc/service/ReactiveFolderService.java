package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.QuickDocFolder;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.web.domain.WebFolder;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveFolderService {

    /**
     * 新增文件目录, 默认上级目录ID为0；
     *
     * @param path
     * @param parentId
     * @param authorizations
     * @return
     */
    Mono<QuickDocFolder> save(String path, ObjectId parentId, Boolean openAccess, AccessAuthorization[] authorizations);

    Mono<QuickDocFolder> save(ObjectId folderId, String path, Boolean openAccess, AccessAuthorization[] authorizations);

    /**
     * 新增文件目录;
     *
     * @param quickDocFolder
     * @return
     */
    Mono<QuickDocFolder> save(QuickDocFolder quickDocFolder);

    /**
     * 重命名文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folder
     * @param newPath
     * @return
     */
    Mono<QuickDocFolder> rename(QuickDocFolder folder, String newPath);

    /**
     * 迁移文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folderId 待迁移文件夹ID
     * @param newParentId 新上级目录ID
     * @return
     */
    Mono<QuickDocFolder> move(ObjectId folderId, ObjectId newParentId);

    /**
     * 删除文件目录
     * Mono流内抛出异常 NoSuchElementException
     *
     * @param folderId
     * @return
     */
    Mono<Void> delete(ObjectId folderId);

    /**
     * 根据上级目录ID信息获取子文件目录
     *
     * @param parentId
     * @return
     */
    Flux<WebFolder> findAllByParentIdInWebFormat(ObjectId parentId);

    Flux<QuickDocFolder> findAllByParentId(ObjectId parentId);

    Flux<WebFolder> findAllInWebFormat();

    Flux<QuickDocFolder> findAll();

    /**
     * 获取文件目录
     *
     * @param path     文件路径名
     * @param parentId 上级目录ID
     * @return
     */
    Mono<QuickDocFolder> findByPathAndParentId(String path, ObjectId parentId);

    /**
     * 根据ID获取文件目录信息
     *
     * @param id 文件目录ID
     * @return
     */
    Mono<QuickDocFolder> findById(ObjectId id);

}