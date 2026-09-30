package com.library.requestform.book;

import com.library.enums.DisplayStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request form tạo mới đầu sách.
 * Bắt buộc nhập đầy đủ tất cả các trường.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCreateRequestForm {

    @NotNull(message = "Mã thể loại không được để trống")
    private Long categoryId;

    @NotBlank(message = "Mã ISBN không được để trống")
    @Size(max = 20, message = "Mã ISBN tối đa 20 ký tự")
    private String isbn;

    @NotBlank(message = "Tiêu đề sách không được để trống")
    @Size(max = 255, message = "Tiêu đề sách tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Size(max = 150, message = "Tên tác giả tối đa 150 ký tự")
    private String author;

    @NotBlank(message = "Nhà xuất bản không được để trống")
    @Size(max = 150, message = "Nhà xuất bản tối đa 150 ký tự")
    private String publisher;

    @NotNull(message = "Năm xuất bản không được để trống")
    @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
    private Integer publicationYear;

    @NotBlank(message = "Mô tả sách không được để trống")
    private String description;

    @NotBlank(message = "Ảnh bìa sách không được để trống")
    @Size(max = 255, message = "Khóa ảnh bìa tối đa 255 ký tự")
    private String coverImageKey;

    @NotNull(message = "Giá sách không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sách phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Giá mượn không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá mượn không được âm")
    private BigDecimal rentalPrice;

    @NotNull(message = "Tiền phạt không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tiền phạt không được âm")
    private BigDecimal fineAmount;

    @NotNull(message = "Trạng thái hiển thị không được để trống")
    private DisplayStatus status;
}
