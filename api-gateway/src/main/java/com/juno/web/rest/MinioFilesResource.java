package com.juno.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import com.juno.config.MinioConfiguration;

import io.github.jhipster.web.util.HeaderUtil;
import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.messages.Item;

@ConditionalOnProperty(prefix = "minio", name = { "endpoint", "bucket-name" })
@Controller
@RequestMapping("/api")
public class MinioFilesResource {

	private final Logger log = LoggerFactory.getLogger(MinioFilesResource.class);

	@Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioConfiguration minioConfig;
    
    @PostConstruct
    public void initialized() {
    	log.info("=== MINIO ENDPOINT STARTED: /api/minio");
    }
    
    /**
     * Browse available object under one bucket
     * @param bucketName
     * @return
     * @throws Exception
     */
    @GetMapping("/minio/{bucketName}")
    public ResponseEntity<List<Item>> browseFiles(@PathVariable String bucketName) throws Exception {
    	List<Item> results = StreamUtils.createStreamFromIterator(minioClient.listObjects(bucketName).iterator())
    	.map(result -> {
    		try {
    			return result.get();
    		} catch (Exception e) {
    			return null;
    		}
    	}).filter(i -> i != null)
    	.collect(Collectors.toList());
    	
    	return ResponseEntity.ok(results);
    }

    /**
     * Upload file to default bucket
     * @param name
     * @param alt
     * @param file
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws InvalidKeyException
     * @throws InvalidBucketNameException
     * @throws NoSuchAlgorithmException
     * @throws NoResponseException
     * @throws ErrorResponseException
     * @throws InternalException
     * @throws InvalidArgumentException
     * @throws InsufficientDataException
     * @throws InvalidResponseException
     * @throws XmlPullParserException
     * @throws RegionConflictException
     */
    @PostMapping("/minio")
	public ResponseEntity<String> putMinio(
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "alt", required = false) String alt, 
			@RequestParam("file") MultipartFile file)
			throws Exception {
    	return putMinioWithBucket(minioConfig.getBucketName(), name, alt, file);
	}

    /**
     * Upload file to any bucket
     * @param bucketName
     * @param name
     * @param alt
     * @param file
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws InvalidKeyException
     * @throws InvalidBucketNameException
     * @throws NoSuchAlgorithmException
     * @throws NoResponseException
     * @throws ErrorResponseException
     * @throws InternalException
     * @throws InvalidArgumentException
     * @throws InsufficientDataException
     * @throws InvalidResponseException
     * @throws XmlPullParserException
     * @throws RegionConflictException
     */
    @PostMapping("/minio/{bucketName}")
	public ResponseEntity<String> putMinioWithBucket(
			@PathVariable String bucketName,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "alt", required = false) String alt, 
			@RequestBody MultipartFile file
	)
			throws Exception {
    	name = (name == null ? file.getOriginalFilename() : name);
    	String basename = FilenameUtils.getBaseName(name).replaceAll("\\W+", "");
    	String ext = FilenameUtils.getExtension(name);
    	name = basename + "." + ext;
    	if (!minioClient.bucketExists(bucketName)) {
    		minioClient.makeBucket(bucketName);
    	}
    	int num = 0;
    	while(lookupObject(bucketName, name)) {
    	    name = basename + (num++) + "." + ext;
    	}
    	log.debug("REST request to save Image : {}", name);
    	try (InputStream is = file.getInputStream()) {
    		minioClient.putObject(bucketName, name , is, new PutObjectOptions(file.getSize(), -1));
    		is.close();
    	} catch (Exception e) {
    		log.error("cannot open input stream", e);
    	}
		return ResponseEntity.created(new URI("api/minio/?name=" + name))
                .headers(HeaderUtil.createAlert(applicationName,  "minio.created", name))
                .body(name);
	}

    /**
     * Proxy file back to client
     * @param name
     * @param alt
     * @param file
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws InvalidKeyException
     * @throws InvalidBucketNameException
     * @throws NoSuchAlgorithmException
     * @throws NoResponseException
     * @throws ErrorResponseException
     * @throws InternalException
     * @throws InvalidArgumentException
     * @throws InsufficientDataException
     * @throws InvalidResponseException
     * @throws XmlPullParserException
     */
    @PostMapping("/public/upload-file")
	public ResponseEntity<String> uploadToMinio(
			@RequestParam(value = "bucket", required = false) String bucketName,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "alt", required = false) String alt,
			@RequestBody MultipartFile file,
			@RequestParam Map<String, String> params)
		throws Exception {
    	name = (name == null ? file.getOriginalFilename() : name);
    	bucketName = bucketName == null ? minioConfig.getBucketName() : bucketName;
    	String basename = FilenameUtils.getBaseName(name).replaceAll("\\W+", "");
    	String ext = FilenameUtils.getExtension(name);
    	name = basename + "." + ext;
    	if (!minioClient.bucketExists(bucketName)) {
    		minioClient.makeBucket(bucketName);
    	}
    	int num = 0;
    	while(lookupObject(bucketName, name)) {
    	    name = basename + (num++) + "." + ext;
    	}
    	log.debug("REST request to save file : {}", name);
    	try (InputStream is = file.getInputStream()) {
    		minioClient.putObject(bucketName, name , is, new PutObjectOptions(file.getSize(), -1));
    		is.close();
    	} catch (Exception e) {
    		log.error("cannot open input stream", e);
    	}
		String result = minioClient.getObjectUrl(bucketName, name);
		return ResponseEntity.created(new URI(result))
                .headers(HeaderUtil.createAlert(applicationName,  "minio.created", name))
                .body(name);
	}
    
    /**
     * Download a file from Minio back to client
     * @param name
     * @return
     * @throws Exception
     */
    @GetMapping("/public/download-file")
    public ResponseEntity<InputStreamResource> downloadObject(@RequestParam String name) throws Exception {
    	ObjectStat objStat = minioClient.statObject(minioConfig.getBucketName(), name);
    	
    	log.debug("REST request to download file: {} type {}", name, objStat.contentType());
    	return ResponseEntity.ok()
    			.header(HttpHeaders.CONTENT_TYPE, objStat.contentType())
    			.header("Content-Disposition", "attachment; filename=" + name)
    			.header(HttpHeaders.ETAG, objStat.etag())
    			.body(new InputStreamResource(minioClient.getObject(minioConfig.getBucketName(), name)));
    }
    
    @GetMapping("/public/static/{name}")
    public ResponseEntity<InputStreamResource> downloadStatic(@PathVariable String name) throws Exception {
    	return downloadObject(name);
    }
    
    @RequestMapping(path = "/public/static/{name}", method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<String> uploadFile(@PathVariable String name, @RequestBody String body) throws Exception {
    	String bucketName = minioConfig.getBucketName();
    	if (!minioClient.bucketExists(bucketName)) {
    		minioClient.makeBucket(bucketName);
    	}
    	ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());
		minioClient.putObject(bucketName, name , bais, new PutObjectOptions(bais.available(), -1));
		bais.close();
		log.debug("REST request to save object : {}", name);
		return ResponseEntity.created(new URI("api/public/static/" + name))
                .headers(HeaderUtil.createAlert(applicationName,  "minio.created", name))
                .body(name);
    }

    /**
     * Generate a 302 redirect to temporary file URL
     * @param name
     * @return
     * @throws Exception
     */
    @GetMapping("/files/download-link")
	public ResponseEntity<String> getDownloadLink(@RequestParam String name) throws Exception {
		return ResponseEntity.status(HttpStatus.FOUND).header("Location", minioClient.presignedGetObject(minioConfig.getBucketName(), name)).build();
	}

    @GetMapping("/files/upload-link")
	public ResponseEntity<String> getObject(@RequestParam String name) throws Exception {
		return ResponseEntity.status(HttpStatus.FOUND).header("Location", minioClient.presignedPutObject(minioConfig.getBucketName(), name)).build();
	}

	public boolean lookupObject(String bucketName, String name)  {
		try {
			minioClient.statObject(bucketName, name);
			return true;
		} catch (ErrorResponseException e) {
			ErrorCode code = e.errorResponse().errorCode();
			if (code != ErrorCode.NO_SUCH_KEY && code != ErrorCode.NO_SUCH_OBJECT) {
				log.error("Other error occur.", e);
			}
		} catch (Exception e) {
			log.error("Other error occur.", e);
		}
		return false;
	}
    
}
