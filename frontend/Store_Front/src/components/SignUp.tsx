import { Box, Button, Card, Center, Input, Stack } from "@chakra-ui/react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { Field } from "../components/ui/field";
import PostSender from "./RESTFul/PostSender";
import { UserEndPoint } from "./services/EndPoints";
import { PasswordInput } from "./ui/password-input";
import { toaster } from "./ui/toaster";

const schema = z
  .object({
    Username: z.string().min(1, { message: "Username is required" }),
    EmailAddress: z.string().email({ message: "Please enter a valid email." }),
    Password: z.string().min(1, { message: "Please input password." }),
    confirmPassword: z
      .string()
      .min(1, { message: "Please confirm your password." }),
  })
  .refine((data) => data.Password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
const ENDPOINT = UserEndPoint;

type FormData = z.infer<typeof schema>;

const SignUp = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const [status, setStatus] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  //这个email是用来跳转页面用的
  const [email, setEmail] = useState("");

  //navigate
  const navigate = useNavigate();

  //用useEffect来提示服务器的相应
  useEffect(() => {
    if (status === 200) {
      toaster.create({
        title: "Product added to cart.",
        type: "success",
      });
      navigate("/");
    } else if (!message)
      toaster.create({
        title: "Somthing wrong.",
        type: "error",
      });
    else if (!error)
      toaster.create({
        title: "Somthing wrong.",
        type: "error",
      });
  }, [status, message, error]);

  const onSubmit = (data: FormData) => {
    const { confirmPassword, ...filteredData } = data;
    PostSender(ENDPOINT, filteredData, setStatus, setMessage, setError);
    console.log(filteredData);
    //这个跳转只是用来测试的
    // navigate("/");
  };

  return (
    <Box height="100vh" width="100vw">
      <Center>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Card.Root maxW="sm">
            <Card.Header>
              <Card.Title>Create Account</Card.Title>
            </Card.Header>
            <Card.Body>
              <Stack gap="4" w="full">
                <Field label="Email" invalid={!!errors.EmailAddress}>
                  <Input
                    {...register("EmailAddress", {
                      required: "email is required",
                    })}
                    id="email"
                    type="email"
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.EmailAddress?.message}
                  </Box>
                </Field>
                <Field label="Username" invalid={!!errors.Username}>
                  <Input
                    {...register("Username", {
                      required: "Username is required",
                    })}
                    id="username"
                    type="text"
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.Username?.message}
                  </Box>
                </Field>

                <Field label="Password" invalid={!!errors.Password}>
                  <PasswordInput
                    {...register("Password", {
                      required: "password is required",
                    })}
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.Password?.message}
                  </Box>
                </Field>
                <Field
                  label="Confirm Password"
                  invalid={!!errors.confirmPassword}
                >
                  <PasswordInput
                    {...register("confirmPassword", {
                      required: "Please repeat your password.",
                    })}
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.confirmPassword?.message}
                  </Box>
                </Field>
              </Stack>
            </Card.Body>
            <Card.Footer justifyContent="flex-end">
              <Button
                color="black"
                onClick={() => {
                  navigate("/");
                }}
              >
                Back
              </Button>
              <Button type="submit" color="black">
                Sign Up
              </Button>
            </Card.Footer>
          </Card.Root>
        </form>
      </Center>
    </Box>
  );
};

export default SignUp;
